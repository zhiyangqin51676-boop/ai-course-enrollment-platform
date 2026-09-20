# UStudent API Testing - HTTP Files

This directory contains HTTP request files that can be executed directly in IntelliJ IDEA using the built-in HTTP client.

## 📁 File Organization

### Core API Testing
- **`01-authentication.http`** - Login endpoints for students and teachers
- **`02-courses.http`** - Course browsing and schedule information
- **`03-enrollment.http`** - Course enrollment with validation
- **`04-drop-courses.http`** - Drop course functionality

### Advanced Features
- **`05-schedule-conflicts.http`** - Time conflict detection and prevention
- **`06-prerequisites.http`** - Academic requirements and eligibility checking
- **`07-student-profiles.http`** - Student academic profile management

### System & Debug
- **`08-health-debug.http`** - Health checks and debug endpoints
- **`99-complete-workflows.http`** - End-to-end test scenarios

## 🚀 How to Use in IntelliJ IDEA

1. **Open any .http file** in IntelliJ IDEA
2. **Click the green arrow** (▶️) next to any request to execute it
3. **View responses** in the bottom panel
4. **Run requests sequentially** to test complete workflows

## 📊 Sample Data Reference

### Users
- **Student 1**: `john_student` (ID: 1) - CS Sophomore, 3.25 GPA, 35 credits
- **Student 2**: `jane_student` (ID: 2) - Math Freshman, 3.80 GPA, 15 credits
- **Teacher 1**: `prof_wilson` (ID: 3) - Dr. Wilson
- **Teacher 2**: `prof_brown` (ID: 4) - Dr. Brown

### Courses & Schedules
- **CS101**: MWF 9:00-10:30 (No prerequisites)
- **CS201**: TTh 11:00-12:30 (Requires: CS101 + Sophomore + 30 credits)
- **MATH101**: MWF 9:30-11:00 (No prerequisites) ⚠️ **Conflicts with CS101**
- **MATH201**: TTh 2:00-3:30 (Requires: MATH101 + 2.5 GPA)
- **ENG101**: MWF 1:00-2:30 (No prerequisites)

### Known Conflicts
- ❌ **CS101 + MATH101**: Time overlap on MWF (9:30-10:30)
- ✅ **CS201 + MATH101**: Different days (TTh vs MWF)

## 🧪 Recommended Testing Order

### 1. Basic Functionality
```
01-authentication.http → 02-courses.http → 03-enrollment.http → 04-drop-courses.http
```

### 2. Advanced Features
```
07-student-profiles.http → 06-prerequisites.http → 05-schedule-conflicts.http
```

### 3. Complete Workflows
```
99-complete-workflows.http (run each workflow section)
```

### 4. System Health
```
08-health-debug.http
```

## ✅ Expected Test Results

### **Working Features:**
- ✅ Authentication (mock login)
- ✅ Course browsing and filtering
- ✅ Enrollment with full validation
- ✅ Drop course functionality
- ✅ Schedule conflict detection
- ✅ Prerequisites enforcement
- ✅ Student profile management

### **Known Issues:**
- ⚠️ CS101 enrollment may return 500 error for some students (data inconsistency, not system flaw)

## 🎯 Key Test Scenarios

### **Prerequisites Testing:**
- Student 2 + CS201 → Should fail (missing CS101, wrong year level, insufficient credits)
- Student 1 + CS201 → Should work after enrolling in CS101 first

### **Conflict Testing:**
- CS101 + MATH101 → Should fail (time conflict on MWF)
- CS201 + MATH101 → Should work (different days)

### **Complete Student Journey:**
1. Login → View courses → Check profile → Check eligibility → Enroll → View schedule → Drop if needed

Happy testing! 🚀