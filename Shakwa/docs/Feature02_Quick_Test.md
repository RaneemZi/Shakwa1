# اختبار سريع - Feature 02

## متطلبات سريعة
1. Postman أو Thunder Client أو curl
2. JWT tokens لموظفين مختلفين من نفس الـ agency
3. شكوى موجودة في النظام

---

## اختبار سريع (5 دقائق)

### الخطوة 1: إنشاء شكوى (إذا لم تكن موجودة)

```bash
POST http://localhost:13000/api/v1/complaints
Authorization: Bearer <citizen_token>
Content-Type: multipart/form-data

data: {
  "complaintType": "تأخر_في_إنجاز_معاملة",
  "governorate": "دمشق",
  "governmentAgency": "وزارة_الصحة",
  "location": "مكتب الخدمات",
  "description": "شكوى اختبار"
}
```

**احفظ:** `id` و `version` من الرد

---

### الخطوة 2: اختبار Optimistic Locking

#### 2.1 - موظف 1 يقرأ الشكوى
```bash
GET http://localhost:13000/api/v1/complaints/{id}
Authorization: Bearer <employee1_token>
```
**احفظ:** `version` (مثلاً: `0`)

#### 2.2 - موظف 2 يحدث الشكوى
```bash
PUT http://localhost:13000/api/v1/complaints/{id}
Authorization: Bearer <employee2_token>
Content-Type: application/json

{
  "complaintType": "تأخر_في_إنجاز_معاملة",
  "governorate": "دمشق",
  "governmentAgency": "وزارة_الصحة",
  "location": "مكتب الخدمات - محدث",
  "description": "تم التحديث من موظف 2"
}
```
**النتيجة:** ✅ نجاح - `version` يصبح `1`

#### 2.3 - موظف 1 يحاول التحديث بـ version قديم
```bash
PUT http://localhost:13000/api/v1/complaints/{id}
Authorization: Bearer <employee1_token>
Content-Type: application/json

{
  "complaintType": "تأخر_في_إنجاز_معاملة",
  "governorate": "دمشق",
  "governmentAgency": "وزارة_الصحة",
  "location": "مكتب الخدمات - من موظف 1",
  "description": "من موظف 1"
}
```
**النتيجة المتوقعة:** ❌ `409 CONFLICT` مع رسالة: "تم تعديل هذه الشكوى من قبل موظف آخر..."

---

### الخطوة 3: اختبار State-Based Locking

#### 3.1 - موظف 1 يبدأ العمل على الشكوى
```bash
PUT http://localhost:13000/api/v1/complaints/{id}/respond?status=IN_PROGRESS&response=بدأت المعالجة
Authorization: Bearer <employee1_token>
```
**النتيجة:** ✅ نجاح - `status` يصبح `IN_PROGRESS`, `respondedBy` = employee1

#### 3.2 - موظف 2 يحاول التعديل (في نفس الوقت)
```bash
PUT http://localhost:13000/api/v1/complaints/{id}
Authorization: Bearer <employee2_token>
Content-Type: application/json

{
  "complaintType": "تأخر_في_إنجاز_معاملة",
  "governorate": "دمشق",
  "governmentAgency": "وزارة_الصحة",
  "location": "مكتب الخدمات - من موظف 2",
  "description": "من موظف 2"
}
```

**النتيجة المتوقعة:**
- ❌ `423 LOCKED` مع رسالة: "الشكوى قيد المعالجة من قبل أحمد محمد"

---

## ✅ علامات النجاح

1. ✅ عند conflict: HTTP `409 CONFLICT` (Optimistic Lock)
2. ✅ عند lock: HTTP `423 LOCKED` (State-Based Lock)
3. ✅ رسالة خطأ بالعربية
4. ✅ `version` يزيد بعد كل تحديث ناجح
5. ✅ State-Based Lock يمنع التعديل عندما Status = IN_PROGRESS

---

## ❌ إذا فشل الاختبار

### المشكلة: لا يوجد conflict عند concurrent update
**الحل:** تأكد من:
- استخدام `findByIdAndAgencyForUpdate()` في Service
- وجود `@Version` في Entity
- استخدام `@Transactional` في Service methods

### المشكلة: Pessimistic lock لا يعمل
**الحل:** تأكد من:
- استخدام `@Lock(LockModeType.PESSIMISTIC_WRITE)` في Repository
- الـ transaction لم ينته بعد
- قاعدة البيانات تدعم SELECT FOR UPDATE

---

## نصائح

1. استخدم Postman **Runner** لاختبار concurrent requests
2. استخدم **2 terminal windows** لاختبار Pessimistic Lock
3. راقب **application logs** لرؤية الـ locks
4. تحقق من **version** في كل response

