# اختبار سريع - Feature 03

## ⚡ اختبار سريع (3 دقائق)

### الخطوة 1: إنشاء شكوى والتحقق من التاريخ

```bash
# 1. إنشاء شكوى
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

**احفظ:** `complaintId` من الرد

```bash
# 2. التحقق من التاريخ
GET http://localhost:13000/api/v1/complaints/{complaintId}/history
Authorization: Bearer <citizen_token>
```

**النتيجة المتوقعة:**
- ✅ سجل واحد: `CREATED`
- ✅ `actionDescription` بالعربية
- ✅ `actorName` = اسم المواطن

---

### الخطوة 2: تغيير الحالة والتحقق من التاريخ

```bash
# 1. موظف يغير الحالة
PUT http://localhost:13000/api/v1/complaints/{complaintId}/respond?response=بدأت&status=IN_PROGRESS
Authorization: Bearer <employee_token>
```

```bash
# 2. التحقق من التاريخ
GET http://localhost:13000/api/v1/complaints/{complaintId}/history
Authorization: Bearer <citizen_token>
```

**النتيجة المتوقعة:**
- ✅ سجلان جديدان: `STATUS_CHANGED` و `LOCKED`
- ✅ `oldValue` = "PENDING"
- ✅ `newValue` = "IN_PROGRESS"

---

## ✅ علامات النجاح

1. ✅ عند إنشاء شكوى: سجل `CREATED` يظهر
2. ✅ عند تغيير الحالة: سجل `STATUS_CHANGED` يظهر
3. ✅ عند تحديث الحقول: سجل `UPDATED_FIELDS` يظهر
4. ✅ عند إضافة مرفق: سجل `ATTACHMENT_ADDED` يظهر
5. ✅ الترتيب: من الأحدث إلى الأقدم
6. ✅ الوصف: بالعربية وواضح

---

## ❌ إذا فشل الاختبار

### المشكلة: لا يظهر أي سجل

**الحل:**
- تحقق من Migration: `V3__add_complaint_history.sql`
- تحقق من logs: "Recorded CREATED history"

### المشكلة: actionDescription فارغ

**الحل:**
- تحقق من `ComplaintHistoryService.generateActionDescription()`
- تحقق من logs للأخطاء

---

**راجع:** `docs/Feature03_Testing_Guide.md` للاختبارات الشاملة

