# دليل اختبار Feature 02 - Concurrency Control

## نظرة عامة
هذا الدليل يوضح كيفية اختبار نظام التحكم في التزامن باستخدام Optimistic و Pessimistic Locking.

---

## 1. اختبار Optimistic Locking (القفل التفاؤلي)

### السيناريو: محاولة تعديل شكوى تم تعديلها من قبل موظف آخر

#### الخطوات:

1. **إنشاء شكوى جديدة:**
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

**احفظ:** `complaintId` و `version` من الرد

2. **موظف 1 - قراءة الشكوى:**
```bash
GET http://localhost:13000/api/v1/complaints/{complaintId}
Authorization: Bearer <employee1_token>
```

**احفظ:** `version` من الرد (مثلاً: `version: 0`)

3. **موظف 2 - تعديل الشكوى (يحدث version):**
```bash
PUT http://localhost:13000/api/v1/complaints/{complaintId}
Authorization: Bearer <employee2_token>
Content-Type: application/json

{
  "complaintType": "تأخر_في_إنجاز_معاملة",
  "governorate": "دمشق",
  "governmentAgency": "وزارة_الصحة",
  "location": "مكتب الخدمات - محدث",
  "description": "شكوى اختبار - تم التحديث من موظف 2"
}
```

**النتيجة المتوقعة:** ✅ نجاح - `version` يصبح `1`

4. **موظف 1 - محاولة التعديل باستخدام version القديم:**
```bash
PUT http://localhost:13000/api/v1/complaints/{complaintId}
Authorization: Bearer <employee1_token>
Content-Type: application/json

{
  "complaintType": "تأخر_في_إنجاز_معاملة",
  "governorate": "دمشق",
  "governmentAgency": "وزارة_الصحة",
  "location": "مكتب الخدمات - من موظف 1",
  "description": "شكوى اختبار - من موظف 1"
}
```

**النتيجة المتوقعة:** ❌ خطأ
- **HTTP Status:** `409 CONFLICT`
- **Message:** `"تم تعديل هذه الشكوى من قبل موظف آخر. يرجى تحديث الصفحة والمحاولة مرة أخرى."`

---

## 2. اختبار State-Based Locking (القفل الوهمي)

### السيناريو: موظفان يحاولان العمل على نفس الشكوى

#### الطريقة 1: استخدام Postman/Thunder Client (Terminal 1 و Terminal 2)

**Terminal 1 - موظف 1 يبدأ العمل:**
```bash
PUT http://localhost:13000/api/v1/complaints/{complaintId}/respond?status=IN_PROGRESS&response=بدأت المعالجة
Authorization: Bearer <employee1_token>
```

**Terminal 2 - موظف 2 يحاول التعديل (في نفس الوقت):**
```bash
PUT http://localhost:13000/api/v1/complaints/{complaintId}
Authorization: Bearer <employee2_token>
Content-Type: application/json

{
  "complaintType": "تأخر_في_إنجاز_معاملة",
  "governorate": "دمشق",
  "governmentAgency": "وزارة_الصحة",
  "location": "مكتب الخدمات - من موظف 2",
  "description": "تحديث من موظف 2"
}
```

**النتيجة المتوقعة:**
- ✅ Terminal 1: نجاح - Status = IN_PROGRESS, respondedBy = employee1
- ❌ Terminal 2: `423 LOCKED` - "الشكوى قيد المعالجة من قبل أحمد محمد"

#### الطريقة 2: اختبار باستخدام curl في terminalين منفصلين

**Terminal 1:**
```bash
curl -X PUT "http://localhost:13000/api/v1/complaints/1/respond?status=IN_PROGRESS&response=بدأت" \
  -H "Authorization: Bearer <employee1_token>"
```

**Terminal 2 (في نفس الوقت):**
```bash
curl -X PUT "http://localhost:13000/api/v1/complaints/1" \
  -H "Authorization: Bearer <employee2_token>" \
  -H "Content-Type: application/json" \
  -d '{"description": "Update from Employee 2"}'
```

---

## 3. اختبار التحديث مع State-Based + Pessimistic + Optimistic Lock

### السيناريو: موظفان يحاولان تحديث نفس الشكوى

**Terminal 1 - موظف 1:**
```bash
PUT http://localhost:13000/api/v1/complaints/{complaintId}
Authorization: Bearer <employee1_token>
Content-Type: application/json

{
  "complaintType": "تأخر_في_إنجاز_معاملة",
  "governorate": "دمشق",
  "governmentAgency": "وزارة_الصحة",
  "location": "مكتب الخدمات - من موظف 1",
  "description": "تحديث من موظف 1"
}
```

**Terminal 2 - موظف 2 (في نفس الوقت):**
```bash
PUT http://localhost:13000/api/v1/complaints/{complaintId}
Authorization: Bearer <employee2_token>
Content-Type: application/json

{
  "complaintType": "تأخر_في_إنجاز_معاملة",
  "governorate": "دمشق",
  "governmentAgency": "وزارة_الصحة",
  "location": "مكتب الخدمات - من موظف 2",
  "description": "تحديث من موظف 2"
}
```

**النتيجة المتوقعة:**
- ✅ Terminal 1: نجاح التحديث (Pessimistic lock يحجز الصف)
- ⏳ Terminal 2: **ينتظر** حتى ينتهي Terminal 1 (Pessimistic lock)، ثم:
  - إذا كان version لم يتغير: ✅ نجاح
  - إذا كان version تغير: ❌ `409 CONFLICT` (Optimistic Lock)

---

## 4. اختبار الرد على الشكوى مع Locking

### السيناريو: موظفان يحاولان الرد على نفس الشكوى

**Terminal 1:**
```bash
PUT http://localhost:13000/api/v1/complaints/{complaintId}/respond?response=رد من موظف 1&status=IN_PROGRESS
Authorization: Bearer <employee1_token>
```

**Terminal 2 (في نفس الوقت):**
```bash
PUT http://localhost:13000/api/v1/complaints/{complaintId}/respond?response=رد من موظف 2&status=RESOLVED
Authorization: Bearer <employee2_token>
```

**النتيجة المتوقعة:**
- ✅ Terminal 1: نجاح
- ⏳ Terminal 2: ينتظر ثم إما نجاح أو `409 CONFLICT`

---

## 5. اختبار باستخدام Postman Collection

### إنشاء Postman Collection:

```json
{
  "info": {
    "name": "Feature02 Concurrency Tests",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "1. Create Complaint",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Authorization",
            "value": "Bearer {{citizen_token}}"
          }
        ],
        "url": {
          "raw": "http://localhost:13000/api/v1/complaints",
          "protocol": "http",
          "host": ["localhost"],
          "port": "13000",
          "path": ["api", "v1", "complaints"]
        },
        "body": {
          "mode": "formdata",
          "formdata": [
            {
              "key": "data",
              "value": "{\"complaintType\":\"تأخر_في_إنجاز_معاملة\",\"governorate\":\"دمشق\",\"governmentAgency\":\"وزارة_الصحة\",\"location\":\"مكتب الخدمات\",\"description\":\"شكوى اختبار\"}",
              "type": "text"
            }
          ]
        }
      }
    },
    {
      "name": "2. Employee 1 - Start Processing (State-Based Lock)",
      "request": {
        "method": "PUT",
        "header": [
          {
            "key": "Authorization",
            "value": "Bearer {{employee1_token}}"
          }
        ],
        "url": {
          "raw": "http://localhost:13000/api/v1/complaints/{{complaintId}}/respond?status=IN_PROGRESS&response=بدأت المعالجة",
          "protocol": "http",
          "host": ["localhost"],
          "port": "13000",
          "path": ["api", "v1", "complaints", "{{complaintId}}", "respond"],
          "query": [
            {"key": "status", "value": "IN_PROGRESS"},
            {"key": "response", "value": "بدأت المعالجة"}
          ]
        }
      }
    },
    {
      "name": "3. Employee 2 - Try to Update (Should Fail with 423)",
      "request": {
        "method": "PUT",
        "header": [
          {
            "key": "Authorization",
            "value": "Bearer {{employee2_token}}"
          },
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "url": {
          "raw": "http://localhost:13000/api/v1/complaints/{{complaintId}}",
          "protocol": "http",
          "host": ["localhost"],
          "port": "13000",
          "path": ["api", "v1", "complaints", "{{complaintId}}"]
        },
        "body": {
          "mode": "raw",
          "raw": "{\"description\": \"Update from Employee 2\"}"
        }
      }
    }
  ]
}
```

---

## 6. اختبار باستخدام Java/Integration Tests

### مثال على Unit Test:

```java
@SpringBootTest
@Transactional
class ComplaintConcurrencyTest {

    @Autowired
    private ComplaintService complaintService;
    
    @Autowired
    private ComplaintRepository complaintRepository;
    
    @Test
    void testOptimisticLocking() {
        // Create complaint
        Complaint complaint = createTestComplaint();
        Long version1 = complaint.getVersion();
        
        // Employee 1 reads
        Complaint complaint1 = complaintRepository.findById(complaint.getId()).orElseThrow();
        
        // Employee 2 updates (changes version)
        complaint.setDescription("Updated by Employee 2");
        complaintRepository.save(complaint);
        
        // Employee 1 tries to update with old version
        complaint1.setDescription("Updated by Employee 1");
        
        // Should throw OptimisticLockException
        assertThrows(OptimisticLockException.class, () -> {
            complaintRepository.save(complaint1);
        });
    }
    
    @Test
    void testPessimisticLocking() throws InterruptedException {
        Complaint complaint = createTestComplaint();
        Long complaintId = complaint.getId();
        
        // Start two threads trying to edit simultaneously
        CountDownLatch latch = new CountDownLatch(2);
        AtomicInteger successCount = new AtomicInteger(0);
        
        // Thread 1 - Employee 1 starts processing
        new Thread(() -> {
            try {
                complaintService.respondToComplaint(complaintId, "Started processing", ComplaintStatus.IN_PROGRESS);
                successCount.incrementAndGet();
            } catch (Exception e) {
                // Expected: might get locked by Thread 2
            } finally {
                latch.countDown();
            }
        }).start();
        
        // Thread 2 - Employee 2 tries to update
        new Thread(() -> {
            try {
                ComplaintDTORequest dto = new ComplaintDTORequest();
                dto.setDescription("Update from Employee 2");
                complaintService.updateComplaint(complaintId, dto);
                successCount.incrementAndGet();
            } catch (LockedException e) {
                // Expected: should get 423 LOCKED
            } finally {
                latch.countDown();
            }
        }).start();
        
        latch.await(5, TimeUnit.SECONDS);
        
        // Both should succeed, but one waits
        assertEquals(2, successCount.get());
    }
}
```

---

## 7. اختبار باستخدام Database Queries

### مراقبة Locks في PostgreSQL:

```sql
-- مراقبة الـ locks النشطة
SELECT 
    locktype, 
    relation::regclass, 
    mode, 
    transactionid, 
    pid, 
    granted
FROM pg_locks 
WHERE relation = 'complaints'::regclass;

-- مراقبة الـ transactions النشطة
SELECT 
    pid,
    usename,
    application_name,
    state,
    query,
    query_start
FROM pg_stat_activity
WHERE state = 'active';
```

---

## 8. سيناريوهات الاختبار الموصى بها

### ✅ يجب اختبارها:

1. ✅ **State-Based Lock:** موظف يبدأ العمل (IN_PROGRESS) وموظف آخر يحاول التعديل → 423 LOCKED
2. ✅ **Optimistic Lock Conflict:** موظف يحاول التحديث بـ version قديم → 409 CONFLICT
3. ✅ **Pessimistic Lock Blocking:** موظفان يحدثان في نفس الوقت (واحد ينتظر)
4. ✅ **Sequential Updates:** موظفان يحدثان بشكل متسلسل (يجب أن ينجحا)
5. ✅ **Concurrent Updates:** موظفان يحدثان في نفس الوقت (واحد ينتظر)
6. ✅ **Version Increment:** التحقق من أن version يزيد بعد كل تحديث
7. ✅ **Error Messages:** التحقق من رسائل الخطأ بالعربية
8. ✅ **Lock Release:** عند تغيير Status إلى RESOLVED، يمكن لأي موظف التعديل

---

## 9. نصائح للاختبار

1. **استخدم tokens مختلفة:** تأكد من استخدام tokens لموظفين مختلفين
2. **راقب الـ logs:** تحقق من logs قاعدة البيانات لرؤية الـ locks
3. **استخدم timeouts:** في Postman، زد الـ timeout لرؤية الـ blocking
4. **اختبر في بيئة منفصلة:** لا تختبر على production
5. **استخدم نفس الـ agency:** تأكد أن الموظفين من نفس الـ government agency

---

## 10. التحقق من النتائج

### ✅ علامات النجاح:

- ✅ عند conflict: HTTP 409 CONFLICT (Optimistic Lock)
- ✅ عند lock: HTTP 423 LOCKED (State-Based Lock)
- ✅ رسالة خطأ بالعربية واضحة
- ✅ State-Based Lock يمنع التعديل عندما Status = IN_PROGRESS
- ✅ Pessimistic lock يمنع concurrent access على مستوى قاعدة البيانات
- ✅ Version يزيد بعد كل تحديث
- ✅ لا توجد data corruption

### ❌ علامات الفشل:

- ❌ HTTP 200 مع data corruption
- ❌ رسائل خطأ بالإنجليزية
- ❌ Version لا يتغير
- ❌ Concurrent updates تنجح بدون conflict

---

## 11. أدوات مساعدة

### Postman Environment Variables:
```json
{
  "citizen_token": "your_citizen_jwt_token",
  "employee1_token": "your_employee1_jwt_token",
  "employee2_token": "your_employee2_jwt_token",
  "complaintId": "1",
  "base_url": "http://localhost:13000"
}
```

### curl Scripts:
احفظ في ملف `test_concurrency.sh`:
```bash
#!/bin/bash

COMPLAINT_ID=1
EMPLOYEE1_TOKEN="your_token_here"
EMPLOYEE2_TOKEN="your_token_here"

echo "Employee 1 starting to process complaint..."
curl -X PUT "http://localhost:13000/api/v1/complaints/$COMPLAINT_ID/respond?status=IN_PROGRESS&response=بدأت" \
  -H "Authorization: Bearer $EMPLOYEE1_TOKEN" &

echo "Employee 2 trying to update same complaint..."
curl -X PUT "http://localhost:13000/api/v1/complaints/$COMPLAINT_ID" \
  -H "Authorization: Bearer $EMPLOYEE2_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"description": "Update from Employee 2"}'

wait
```

---

## ملاحظات مهمة

1. **State-Based Lock** يتم تحريره تلقائياً عند تغيير Status إلى RESOLVED/REJECTED/CLOSED
2. **Pessimistic Lock** يتم تحريره تلقائياً عند انتهاء الـ transaction
3. **Optimistic Lock** يتحقق عند `save()` فقط
4. الـ locks تعمل على مستوى قاعدة البيانات، لذا تحتاج PostgreSQL running
5. تأكد من أن الـ transactions قصيرة لتجنب blocking طويل
6. **State-Based Lock** يستخدم `status` و `responded_by` الموجودين بالفعل - لا حاجة لأعمدة إضافية

