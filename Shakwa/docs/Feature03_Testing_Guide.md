# دليل اختبار Feature 03 - Complaint Versioning & History Timeline

## 📋 نظرة عامة

هذا الدليل يوضح كيفية اختبار نظام سجل تغييرات الشكوى (Complaint History) للتأكد من أن جميع التغييرات يتم تسجيلها بشكل صحيح.

---

## 🎯 متطلبات الاختبار

1. **Postman** أو **Thunder Client** أو **curl**
2. **JWT tokens** لـ:
   - مواطن (Citizen)
   - موظف (Employee)
   - مدير (Admin) - اختياري
3. **شكوى موجودة** في النظام (أو إنشاء واحدة جديدة)

---

## ✅ اختبارات سريعة (5-10 دقائق)

### اختبار 1: التحقق من إنشاء سجل عند إنشاء شكوى جديدة

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
  "description": "شكوى اختبار للتاريخ"
}
```

**احفظ:** `complaintId` من الرد

2. **التحقق من التاريخ:**
```bash
GET http://localhost:13000/api/v1/complaints/{complaintId}/history?page=0&size=10
Authorization: Bearer <citizen_token>
```

**النتيجة المتوقعة:**
```json
{
  "content": [
    {
      "id": 1,
      "actionType": "CREATED",
      "actionDescription": "تم إنشاء الشكوى من قبل [اسم المواطن]",
      "actorId": 1,
      "actorName": "[اسم المواطن]",
      "createdAt": "2024-01-15 10:30:00"
    }
  ],
  "totalElements": 1,
  "totalPages": 1
}
```

---

### اختبار 2: التحقق من تسجيل تغيير الحالة

#### الخطوات:

1. **موظف يرد على الشكوى ويغير الحالة:**
```bash
PUT http://localhost:13000/api/v1/complaints/{complaintId}/respond?response=بدأت المعالجة&status=IN_PROGRESS
Authorization: Bearer <employee_token>
```

2. **التحقق من التاريخ:**
```bash
GET http://localhost:13000/api/v1/complaints/{complaintId}/history?page=0&size=10
Authorization: Bearer <citizen_token>
```

**النتيجة المتوقعة:**
```json
{
  "content": [
    {
      "actionType": "STATUS_CHANGED",
      "fieldChanged": "status",
      "oldValue": "PENDING",
      "newValue": "IN_PROGRESS",
      "actionDescription": "تم تغيير حالة الشكوى من 'قيد الانتظار' إلى 'قيد المعالجة' من قبل [اسم الموظف]"
    },
    {
      "actionType": "LOCKED",
      "actionDescription": "تم حجز الشكوى (بدء المعالجة) من قبل [اسم الموظف]"
    },
    {
      "actionType": "CREATED",
      "actionDescription": "تم إنشاء الشكوى من قبل [اسم المواطن]"
    }
  ],
  "totalElements": 3
}
```

---

### اختبار 3: التحقق من تسجيل تحديث الحقول

#### الخطوات:

1. **موظف يحدث وصف الشكوى:**
```bash
PUT http://localhost:13000/api/v1/complaints/{complaintId}
Authorization: Bearer <employee_token>
Content-Type: application/json

{
  "complaintType": "تأخر_في_إنجاز_معاملة",
  "governorate": "دمشق",
  "governmentAgency": "وزارة_الصحة",
  "location": "مكتب الخدمات",
  "description": "تم تحديث الوصف - وصف جديد"
}
```

2. **التحقق من التاريخ:**
```bash
GET http://localhost:13000/api/v1/complaints/{complaintId}/history?page=0&size=10
Authorization: Bearer <citizen_token>
```

**النتيجة المتوقعة:**
```json
{
  "content": [
    {
      "actionType": "UPDATED_FIELDS",
      "fieldChanged": "description",
      "oldValue": "شكوى اختبار للتاريخ",
      "newValue": "تم تحديث الوصف - وصف جديد",
      "actionDescription": "تم تحديث حقل 'الوصف' من قبل [اسم الموظف]"
    },
    ...
  ]
}
```

---

### اختبار 4: التحقق من تسجيل إضافة/حذف المرفقات

#### الخطوات:

1. **مواطن يضيف مرفق:**
```bash
POST http://localhost:13000/api/v1/complaints/{complaintId}/attachments
Authorization: Bearer <citizen_token>
Content-Type: multipart/form-data

files: [اختر ملف]
```

2. **التحقق من التاريخ:**
```bash
GET http://localhost:13000/api/v1/complaints/{complaintId}/history?page=0&size=10
Authorization: Bearer <citizen_token>
```

**النتيجة المتوقعة:**
```json
{
  "content": [
    {
      "actionType": "ATTACHMENT_ADDED",
      "metadata": "{\"fileName\":\"document.pdf\",\"filePath\":\"...\"}",
      "actionDescription": "تم إضافة مرفق 'document.pdf' من قبل [اسم المواطن]"
    },
    ...
  ]
}
```

3. **مواطن يحذف مرفق:**
```bash
DELETE http://localhost:13000/api/v1/complaints/{complaintId}/attachments/{attachmentId}
Authorization: Bearer <citizen_token>
```

4. **التحقق من التاريخ:**
```bash
GET http://localhost:13000/api/v1/complaints/{complaintId}/history?page=0&size=10
Authorization: Bearer <citizen_token>
```

**النتيجة المتوقعة:**
```json
{
  "content": [
    {
      "actionType": "ATTACHMENT_REMOVED",
      "metadata": "{\"fileName\":\"document.pdf\"}",
      "actionDescription": "تم حذف مرفق 'document.pdf' من قبل [اسم المواطن]"
    },
    ...
  ]
}
```

---

### اختبار 5: التحقق من تسجيل LOCKED/UNLOCKED

#### الخطوات:

1. **موظف يبدأ المعالجة (IN_PROGRESS):**
```bash
PUT http://localhost:13000/api/v1/complaints/{complaintId}/respond?response=بدأت&status=IN_PROGRESS
Authorization: Bearer <employee_token>
```

2. **التحقق من التاريخ - يجب أن يحتوي على LOCKED:**
```bash
GET http://localhost:13000/api/v1/complaints/{complaintId}/history?page=0&size=10
Authorization: Bearer <citizen_token>
```

3. **موظف ينتهي من المعالجة (RESOLVED):**
```bash
PUT http://localhost:13000/api/v1/complaints/{complaintId}/respond?response=تم الحل&status=RESOLVED
Authorization: Bearer <employee_token>
```

4. **التحقق من التاريخ - يجب أن يحتوي على UNLOCKED:**
```bash
GET http://localhost:13000/api/v1/complaints/{complaintId}/history?page=0&size=10
Authorization: Bearer <citizen_token>
```

**النتيجة المتوقعة:**
```json
{
  "content": [
    {
      "actionType": "UNLOCKED",
      "actionDescription": "تم تحرير الشكوى (انتهاء المعالجة) من قبل [اسم الموظف]"
    },
    {
      "actionType": "STATUS_CHANGED",
      "oldValue": "IN_PROGRESS",
      "newValue": "RESOLVED"
    },
    {
      "actionType": "LOCKED",
      "actionDescription": "تم حجز الشكوى (بدء المعالجة) من قبل [اسم الموظف]"
    },
    ...
  ]
}
```

---

## 🔒 اختبارات الأمان

### اختبار 6: المواطن يرى فقط تاريخ شكاويه

#### الخطوات:

1. **مواطن 1 يطلب تاريخ شكوى مواطن 2:**
```bash
GET http://localhost:13000/api/v1/complaints/{complaintId_of_citizen2}/history
Authorization: Bearer <citizen1_token>
```

**النتيجة المتوقعة:**
- ❌ `403 FORBIDDEN` أو `401 UNAUTHORIZED`
- رسالة: "You don't have access to this complaint"

---

### اختبار 7: الموظف يرى فقط تاريخ شكاوى جهته

#### الخطوات:

1. **موظف من جهة A يطلب تاريخ شكوى لجهة B:**
```bash
GET http://localhost:13000/api/v1/complaints/{complaintId_of_agencyB}/history
Authorization: Bearer <employee_agencyA_token>
```

**النتيجة المتوقعة:**
- ❌ `403 FORBIDDEN` أو `401 UNAUTHORIZED`
- رسالة: "You don't have access to this complaint"

---

### اختبار 8: المدير يرى كل التاريخ

#### الخطوات:

1. **مدير يطلب تاريخ أي شكوى:**
```bash
GET http://localhost:13000/api/v1/complaints/{any_complaintId}/history
Authorization: Bearer <admin_token>
```

**النتيجة المتوقعة:**
- ✅ `200 OK`
- جميع السجلات متاحة

---

## 📊 اختبارات Pagination

### اختبار 9: Pagination يعمل بشكل صحيح

#### الخطوات:

1. **إنشاء شكوى وتنفيذ عدة إجراءات عليها:**
   - إنشاء
   - تحديث
   - تغيير الحالة
   - إضافة مرفق
   - حذف مرفق

2. **طلب الصفحة الأولى:**
```bash
GET http://localhost:13000/api/v1/complaints/{complaintId}/history?page=0&size=2
Authorization: Bearer <citizen_token>
```

3. **طلب الصفحة الثانية:**
```bash
GET http://localhost:13000/api/v1/complaints/{complaintId}/history?page=1&size=2
Authorization: Bearer <citizen_token>
```

**النتيجة المتوقعة:**
- الصفحة الأولى: آخر إجراءين (الأحدث)
- الصفحة الثانية: الإجراءات السابقة
- `totalElements` = العدد الكلي
- `totalPages` = عدد الصفحات

---

## 🔄 اختبارات متكاملة

### اختبار 10: سيناريو كامل - من الإنشاء إلى الحل

#### الخطوات:

1. **مواطن ينشئ شكوى:**
```bash
POST /api/v1/complaints
→ History: CREATED
```

2. **مواطن يضيف مرفق:**
```bash
POST /api/v1/complaints/{id}/attachments
→ History: ATTACHMENT_ADDED
```

3. **موظف يبدأ المعالجة:**
```bash
PUT /api/v1/complaints/{id}/respond?status=IN_PROGRESS
→ History: STATUS_CHANGED, LOCKED
```

4. **موظف يحدث الوصف:**
```bash
PUT /api/v1/complaints/{id}
→ History: UPDATED_FIELDS
```

5. **موظف يحل الشكوى:**
```bash
PUT /api/v1/complaints/{id}/respond?status=RESOLVED
→ History: STATUS_CHANGED, UNLOCKED
```

6. **التحقق من التاريخ الكامل:**
```bash
GET /api/v1/complaints/{id}/history
```

**النتيجة المتوقعة:**
```json
{
  "content": [
    {
      "actionType": "UNLOCKED",
      "actionDescription": "تم تحرير الشكوى (انتهاء المعالجة) من قبل [اسم الموظف]"
    },
    {
      "actionType": "STATUS_CHANGED",
      "oldValue": "IN_PROGRESS",
      "newValue": "RESOLVED"
    },
    {
      "actionType": "UPDATED_FIELDS",
      "fieldChanged": "description"
    },
    {
      "actionType": "LOCKED",
      "actionDescription": "تم حجز الشكوى (بدء المعالجة) من قبل [اسم الموظف]"
    },
    {
      "actionType": "STATUS_CHANGED",
      "oldValue": "PENDING",
      "newValue": "IN_PROGRESS"
    },
    {
      "actionType": "ATTACHMENT_ADDED",
      "actionDescription": "تم إضافة مرفق 'document.pdf' من قبل [اسم المواطن]"
    },
    {
      "actionType": "CREATED",
      "actionDescription": "تم إنشاء الشكوى من قبل [اسم المواطن]"
    }
  ],
  "totalElements": 7
}
```

**ملاحظة:** الترتيب من الأحدث إلى الأقدم (DESC)

---

## ✅ Checklist للاختبار

- [ ] ✅ CREATED - عند إنشاء شكوى جديدة
- [ ] ✅ STATUS_CHANGED - عند تغيير الحالة
- [ ] ✅ UPDATED_FIELDS - عند تحديث الحقول
- [ ] ✅ ATTACHMENT_ADDED - عند إضافة مرفق
- [ ] ✅ ATTACHMENT_REMOVED - عند حذف مرفق
- [ ] ✅ LOCKED - عند بدء المعالجة (IN_PROGRESS)
- [ ] ✅ UNLOCKED - عند انتهاء المعالجة (RESOLVED/REJECTED/CLOSED)
- [ ] ✅ Security - المواطن يرى فقط شكاويه
- [ ] ✅ Security - الموظف يرى فقط شكاوى جهته
- [ ] ✅ Security - المدير يرى الكل
- [ ] ✅ Pagination - يعمل بشكل صحيح
- [ ] ✅ الترتيب - من الأحدث إلى الأقدم
- [ ] ✅ actionDescription - بالعربية وواضح

---

## 🐛 Troubleshooting

### المشكلة: لا يظهر سجل CREATED عند إنشاء شكوى

**الحل:**
- تحقق من أن `complaintHistoryService.recordCreation()` يتم استدعاؤه في `createComplaint()`
- تحقق من logs: "Recorded CREATED history for complaint ID: X"

---

### المشكلة: التاريخ فارغ

**الحل:**
- تحقق من أن Migration تم تنفيذه: `V3__add_complaint_history.sql`
- تحقق من أن جدول `complaint_history` موجود في Database
- تحقق من logs للأخطاء

---

### المشكلة: Pagination لا يعمل

**الحل:**
- تحقق من أن `Pageable` يتم تمريره بشكل صحيح
- تحقق من أن `findByComplaintIdOrderByCreatedAtDesc` في Repository يعمل

---

### المشكلة: actionDescription غير موجود أو فارغ

**الحل:**
- تحقق من أن `generateActionDescription()` يتم استدعاؤه في `ComplaintHistoryService`
- تحقق من أن `actionDescription` يتم تعيينه قبل `save()`

---

## 📝 ملاحظات مهمة

1. **الترتيب:** التاريخ مرتب من الأحدث إلى الأقدم (DESC)
2. **Immutable:** السجلات لا يمكن تعديلها أو حذفها
3. **Security:** كل مستخدم يرى فقط ما لديه صلاحية للوصول إليه
4. **Pagination:** افتراضي: page=0, size=10
5. **Backfill:** الشكاوى الموجودة قبل Migration لديها سجل CREATED واحد فقط

---

## 🎯 نصائح للاختبار

1. استخدم **Postman Collection** لتنظيم الاختبارات
2. راقب **application logs** لرؤية تسجيل التاريخ
3. استخدم **Database queries** للتحقق مباشرة:
   ```sql
   SELECT * FROM complaint_history 
   WHERE complaint_id = 1 
   ORDER BY created_at DESC;
   ```
4. اختبر **سيناريوهات متعددة** للتأكد من التغطية الكاملة

---

**آخر تحديث:** 2024-01-15

