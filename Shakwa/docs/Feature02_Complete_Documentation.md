# Feature 02 - Concurrency Control & Versioning - التوثيق الشامل

## 📋 جدول المحتويات

1. [نظرة عامة](#نظرة-عامة)
2. [المفاهيم الأساسية](#المفاهيم-الأساسية)
3. [كيف يعمل النظام](#كيف-يعمل-النظام)
4. [Flow كامل](#flow-كامل)
5. [API Endpoints](#api-endpoints)
6. [Database Schema](#database-schema)
7. [كيفية الاختبار](#كيفية-الاختبار)
8. [أمثلة عملية](#أمثلة-عملية)
9. [Troubleshooting](#troubleshooting)

---

## 🎯 نظرة عامة

Feature 02 يمنع تعديل نفس الشكوى من قبل أكثر من موظف في الوقت نفسه باستخدام **ثلاثة مستويات من الحماية**:

1. **State-Based Locking** - استخدام حالة الشكوى (Status) كقفل وهمي
2. **Optimistic Locking** (@Version) - منع التعديلات المتزامنة
3. **Pessimistic Locking** (SELECT FOR UPDATE) - حجز على مستوى قاعدة البيانات

> **ملاحظة:** تم إزالة Application-Level Locking (LockService) لأنه غير منطقي وغير user-friendly. راجع `docs/Feature02_Why_State_Based_Locking.md` للتفاصيل.

---

## 📚 المفاهيم الأساسية

### 1. State-Based Locking (قفل وهمي)

**ما هو؟**
- استخدام حالة الشكوى (Status) كقفل وهمي
- عندما تكون `Status = IN_PROGRESS` و `respondedBy` محدد → الشكوى "محجوزة"
- لا يحتاج أعمدة إضافية في Database

**متى يُستخدم؟**
- تلقائياً عند محاولة التعديل
- عندما يبدأ موظف العمل: `PUT /respond?status=IN_PROGRESS`
- عند انتهاء العمل: `PUT /respond?status=RESOLVED`

**المميزات:**
- ✅ طبيعي - الحالة تعكس الواقع
- ✅ تلقائي - لا حاجة لـ manual lock/unlock
- ✅ بسيط - لا حاجة لـ LockService أو Scheduler
- ✅ مرن - الموظف يتحكم بوقت العمل (لا expiration)
- ✅ المدير يمكنه التعديل دائماً

### 2. Optimistic Locking (@Version)

**ما هو؟**
- آلية JPA تلقائية لمنع التعديلات المتزامنة
- يستخدم عمود `version` الذي يزيد تلقائياً عند كل تحديث
- يتحقق من الـ version قبل الحفظ

**متى يُستخدم؟**
- تلقائياً عند `save()` في JPA
- يمنع التعديلات المتزامنة حتى لو تم تجاوز Application-Level Lock

**المميزات:**
- ✅ تلقائي - لا يحتاج كود إضافي
- ✅ يعمل على مستوى قاعدة البيانات
- ✅ يمنع data corruption

### 3. Pessimistic Locking (SELECT FOR UPDATE)

**ما هو؟**
- حجز على مستوى قاعدة البيانات
- يستخدم `SELECT FOR UPDATE` في SQL
- يحجز الصف حتى انتهاء الـ transaction

**متى يُستخدم؟**
- في `findByIdForUpdate()` و `findByIdAndAgencyForUpdate()`
- عند الحاجة لحجز فوري على مستوى قاعدة البيانات

**المميزات:**
- ✅ يمنع concurrent access على مستوى قاعدة البيانات
- ✅ يعمل عبر multiple application instances
- ✅ يتحرر تلقائياً عند انتهاء transaction

---

## ⚙️ كيف يعمل النظام

### السيناريو الكامل:

```
1. موظف 1 يبدأ العمل على شكوى
   ↓
   PUT /api/v1/complaints/{id}/respond?status=IN_PROGRESS&response=بدأت المعالجة
   ↓
   ComplaintService.respondToComplaint()
   ↓
   - Pessimistic Lock (SELECT FOR UPDATE) - يمنع concurrent access
   - Status → IN_PROGRESS
   - respondedBy → employee1
   - Optimistic Lock (version check) - يمنع concurrent modifications
   ↓
   (هذا يعني: "هذه الشكوى قيد المعالجة من قبل employee1")

2. موظف 2 يحاول تعديل نفس الشكوى
   ↓
   PUT /api/v1/complaints/{id}
   ↓
   ComplaintService.updateComplaint()
   ↓
   - Pessimistic Lock (SELECT FOR UPDATE) - ينتظر حتى transaction 1 ينتهي
   - State-Based Check: Status = IN_PROGRESS && respondedBy != employee2?
   - YES → Throw LockedException: "الشكوى قيد المعالجة من قبل أحمد محمد"
   - NO → Continue

3. موظف 1 ينتهي من العمل
   ↓
   PUT /api/v1/complaints/{id}/respond?status=RESOLVED&response=تم الحل
   ↓
   - Status → RESOLVED
   - (هذا يعني: "انتهيت من العمل" = "تحرير القفل تلقائياً")

4. موظف 2 يحاول التعديل الآن
   ↓
   PUT /api/v1/complaints/{id}
   ↓
   - State-Based Check: Status = IN_PROGRESS? → NO (Status = RESOLVED)
   - Continue → ✅ Success
```

---

## 🔄 Flow كامل

### Flow 1: بدء العمل على شكوى (State-Based Lock)

```
[Employee] → PUT /api/v1/complaints/{id}/respond?status=IN_PROGRESS
    ↓
[ComplaintController] → complaintService.respondToComplaint()
    ↓
[ComplaintService] → 
    ├─ Pessimistic Lock (SELECT FOR UPDATE)
    ├─ State-Based Check: Is locked by other? → YES → Throw LockedException (423)
    ├─ Set Status = IN_PROGRESS
    ├─ Set respondedBy = currentEmployee
    └─ Save (Optimistic Lock check)
    ↓
[Response] → ComplaintDTOResponse with Status = IN_PROGRESS
```

### Flow 2: تحديث شكوى (State-Based + Pessimistic + Optimistic)

```
[Employee] → PUT /api/v1/complaints/{id}
    ↓
[ComplaintController] → complaintService.updateComplaint()
    ↓
[ComplaintService] → 
    ├─ Pessimistic Lock (SELECT FOR UPDATE)
    ├─ Check agency access
    ├─ State-Based Check: Is locked by other?
    │   ├─ Status = IN_PROGRESS && respondedBy != currentEmployee?
    │   ├─ YES → Throw LockedException (423)
    │   └─ NO → Continue
    ├─ Update fields
    └─ complaintRepository.save()
        ├─ JPA checks version
        ├─ If version mismatch → OptimisticLockException
        └─ If version OK → increment version, save
    ↓
[Response] → ComplaintDTOResponse
```

### Flow 3: الرد على شكوى (State-Based + Pessimistic + Optimistic)

```
[Employee] → PUT /api/v1/complaints/{id}/respond?status=IN_PROGRESS
    ↓
[ComplaintController] → complaintService.respondToComplaint()
    ↓
[ComplaintService] → 
    ├─ Pessimistic Lock (SELECT FOR UPDATE)
    ├─ Check agency access
    ├─ State-Based Check: Is locked by other? (if status=IN_PROGRESS)
    ├─ Set response, status, respondedBy, respondedAt
    └─ complaintRepository.save()
        └─ Optimistic locking check (version)
    ↓
[Response] → ComplaintDTOResponse with Status = IN_PROGRESS
```

### Flow 4: انتهاء العمل (تحرير القفل التلقائي)

```
[Employee] → PUT /api/v1/complaints/{id}/respond?status=RESOLVED
    ↓
[ComplaintController] → complaintService.respondToComplaint()
    ↓
[ComplaintService] → 
    ├─ Pessimistic Lock (SELECT FOR UPDATE)
    ├─ Set Status = RESOLVED
    ├─ Set response, respondedBy, respondedAt
    └─ Save (Optimistic Lock check)
    ↓
[Response] → ComplaintDTOResponse with Status = RESOLVED
    ↓
(هذا يعني: "انتهيت من العمل" = "تحرير القفل تلقائياً")
```

---

## 🌐 API Endpoints

### 1. بدء العمل على شكوى (State-Based Lock)

```http
PUT /api/v1/complaints/{id}/respond?status=IN_PROGRESS&response=بدأت المعالجة
Authorization: Bearer <employee_token>
```

**Response:**
```json
{
  "id": 1,
  "status": "IN_PROGRESS",
  "respondedById": 5,
  "respondedByName": "أحمد محمد",
  "version": 2
}
```

**ملاحظة:** هذا يحجز الشكوى تلقائياً (State-Based Lock)

---

### 2. Update Complaint (State-Based Check)

```http
PUT /api/v1/complaints/{id}
Authorization: Bearer <employee_token>
Content-Type: application/json

{
  "complaintType": "تأخر_في_إنجاز_معاملة",
  "governorate": "دمشق",
  "governmentAgency": "وزارة_الصحة",
  "location": "مكتب الخدمات",
  "description": "تحديث جديد"
}
```

**Response:**
```json
{
  "id": 1,
  "status": "PENDING",
  "version": 3
}
```

**Errors:**
- `423 LOCKED` - إذا كانت محجوزة من موظف آخر (Status = IN_PROGRESS)
- `409 CONFLICT` - إذا كان version قديم (OptimisticLockException)

---

### 3. Respond to Complaint (State-Based)

```http
PUT /api/v1/complaints/{id}/respond?response=تم الاطلاع&status=IN_PROGRESS
Authorization: Bearer <employee_token>
```

**Response:**
```json
{
  "id": 1,
  "response": "تم الاطلاع",
  "status": "IN_PROGRESS",
  "respondedById": 5,
  "respondedByName": "أحمد محمد",
  "version": 4
}
```

**Errors:**
- `423 LOCKED` - إذا كانت محجوزة من موظف آخر
- `409 CONFLICT` - إذا كان version قديم

---

## 🗄️ Database Schema

### Complaints Table

```sql
CREATE TABLE complaints (
    id BIGSERIAL PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,  -- Optimistic locking
    status VARCHAR(50) NOT NULL,  -- State-based locking (IN_PROGRESS = locked)
    responded_by BIGINT REFERENCES employees(id),  -- State-based locking (who is processing)
    -- ... other columns
);
```

**ملاحظة:** State-Based Locking يستخدم `status` و `responded_by` الموجودين بالفعل - لا حاجة لأعمدة إضافية!

### Migration File

```sql
-- Add version column for optimistic locking
ALTER TABLE complaints
    ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0 NOT NULL;

UPDATE complaints SET version = 0 WHERE version IS NULL;
```

---

## 🧪 كيفية الاختبار

### اختبار 1: State-Based Locking

**الخطوات:**
1. موظف 1: `PUT /api/v1/complaints/1/respond?status=IN_PROGRESS` → ✅ نجاح
2. موظف 2: `PUT /api/v1/complaints/1` → ❌ `423 LOCKED`
3. موظف 1: `PUT /api/v1/complaints/1/respond?status=RESOLVED` → ✅ نجاح
4. موظف 2: `PUT /api/v1/complaints/1` → ✅ نجاح الآن

**النتيجة المتوقعة:**
- موظف 2 يحصل على `423 LOCKED` مع رسالة: "الشكوى قيد المعالجة من قبل..."

---

### اختبار 2: Optimistic Locking

**الخطوات:**
1. موظف 1: `GET /api/v1/complaints/1` → يحفظ `version: 2`
2. موظف 2: `PUT /api/v1/complaints/1` → ✅ نجاح → `version: 3`
3. موظف 1: `PUT /api/v1/complaints/1` (باستخدام version: 2) → ❌ `409 CONFLICT`

**النتيجة المتوقعة:**
- موظف 1 يحصل على `409 CONFLICT` مع رسالة: "تم تعديل هذه الشكوى من قبل موظف آخر..."

---

### اختبار 3: State-Based Lock on Update

**الخطوات:**
1. موظف 1: `PUT /api/v1/complaints/1/respond?status=IN_PROGRESS` → ✅ نجاح
2. موظف 2: `PUT /api/v1/complaints/1` (في نفس الوقت) → ❌ `423 LOCKED`

**النتيجة المتوقعة:**
- موظف 2 يحصل على `423 LOCKED` لأن Status = IN_PROGRESS

---

### اختبار 4: تحرير القفل التلقائي

**الخطوات:**
1. موظف 1: `PUT /api/v1/complaints/1/respond?status=IN_PROGRESS` → ✅ نجاح
2. موظف 1: `PUT /api/v1/complaints/1/respond?status=RESOLVED` → ✅ نجاح
3. موظف 2: `PUT /api/v1/complaints/1` → ✅ نجاح الآن

**النتيجة المتوقعة:**
- بعد تغيير Status إلى RESOLVED، يمكن لأي موظف التعديل

---

## 💡 أمثلة عملية

### مثال 1: موظف يبدأ العمل على شكوى (State-Based Lock)

```bash
# Step 1: Start processing (State-Based Lock)
curl -X PUT "http://localhost:13000/api/v1/complaints/1/respond?status=IN_PROGRESS&response=بدأت المعالجة" \
  -H "Authorization: Bearer <employee1_token>"

# Response:
{
  "id": 1,
  "status": "IN_PROGRESS",
  "respondedById": 5,
  "respondedByName": "أحمد محمد",
  "version": 2
}

# Step 2: Update complaint (State-Based check happens automatically)
curl -X PUT "http://localhost:13000/api/v1/complaints/1" \
  -H "Authorization: Bearer <employee1_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "complaintType": "تأخر_في_إنجاز_معاملة",
    "governorate": "دمشق",
    "governmentAgency": "وزارة_الصحة",
    "location": "مكتب الخدمات - محدث",
    "description": "تم التحديث"
  }'

# Step 3: Finish work (Lock released automatically)
curl -X PUT "http://localhost:13000/api/v1/complaints/1/respond?status=RESOLVED&response=تم الحل" \
  -H "Authorization: Bearer <employee1_token>"
```

### مثال 2: موظفان يحاولان التعديل

```bash
# Terminal 1 - Employee 1
curl -X PUT "http://localhost:13000/api/v1/complaints/1" \
  -H "Authorization: Bearer <employee1_token>" \
  -H "Content-Type: application/json" \
  -d '{"description": "Update from Employee 1"}'

# Terminal 2 - Employee 2 (في نفس الوقت)
curl -X PUT "http://localhost:13000/api/v1/complaints/1" \
  -H "Authorization: Bearer <employee2_token>" \
  -H "Content-Type: application/json" \
  -d '{"description": "Update from Employee 2"}'

# Result:
# Terminal 1: ✅ Success (version: 2)
# Terminal 2: ❌ 423 LOCKED or 409 CONFLICT
```

---

## 🔧 Troubleshooting

### المشكلة: State-Based Lock لا يعمل

**الأسباب المحتملة:**
1. `ensureNotLockedByState()` method غير موجود في ComplaintService
2. Check logic غير صحيح

**الحل:**
```java
// تأكد من وجود:
private void ensureNotLockedByState(Complaint complaint, Employee currentEmployee, User currentUser) {
    if (complaint.getStatus() == ComplaintStatus.IN_PROGRESS && 
        complaint.getRespondedBy() != null &&
        !complaint.getRespondedBy().getId().equals(currentEmployee.getId()) &&
        !isAdmin(currentUser)) {
        throw new LockedException("...");
    }
}
```

---

### المشكلة: Optimistic Lock لا يعمل

**الأسباب المحتملة:**
1. `@Version` غير موجود في Entity
2. `version` column غير موجود في Database

**الحل:**
```java
// في Complaint.java
@Version
@Column(name = "version", nullable = false)
private Long version;
```

---

### المشكلة: State-Based Lock لا ينتهي

**الأسباب المحتملة:**
1. Status لم يتغير إلى RESOLVED/REJECTED/CLOSED
2. respondedBy ما زال محدد

**الحل:**
- State-Based Lock ينتهي تلقائياً عند تغيير Status إلى RESOLVED/REJECTED/CLOSED
- لا حاجة لـ Scheduler - التحرير تلقائي عند تغيير الحالة

---

## 📊 ملخص

| الميزة | النوع | متى يُستخدم | HTTP Status |
|--------|-------|-------------|-------------|
| State-Based Lock | Status + respondedBy | Check on update/respond | 423 LOCKED |
| Optimistic Lock | @Version | Automatic on save() | 409 CONFLICT |
| Pessimistic Lock | SELECT FOR UPDATE | findByIdForUpdate() | Blocks until transaction ends |

---

## ✅ Checklist للاختبار

- [ ] State-based lock (IN_PROGRESS status)
- [ ] State-based lock release (RESOLVED status)
- [ ] Optimistic lock conflict detection
- [ ] Pessimistic lock blocking
- [ ] Admin override
- [ ] Error messages in Arabic
- [ ] Version increment

---

## 📝 ملاحظات مهمة

1. **State-Based Lock** و **Optimistic Lock** و **Pessimistic Lock** يعملان معاً
2. **State-Based Lock** يستخدم `status` و `responded_by` الموجودين بالفعل
3. Lock يتم تحريره تلقائياً عند تغيير Status إلى RESOLVED/REJECTED/CLOSED
4. **لا حاجة** لـ Scheduled tasks أو LockService
5. Version يزيد تلقائياً من JPA عند كل `save()`
6. **راجع:** `docs/Feature02_Why_State_Based_Locking.md` لمعرفة لماذا State-Based هو الحل الصحيح

---

**آخر تحديث:** 2024-01-15

