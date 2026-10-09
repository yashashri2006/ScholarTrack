# 🎓 ScholarTrack — MahaDBT Scholarship Application Tracker

<div align="center">

<img src="app\src\main\res\drawable\scholartrack_image.jpg" alt="ScholarTrack Logo" width="130" />

### A step-by-step scholarship tracking companion for engineering students

**MahaDBT Eligibility · Document Checklist · Coordinator Verification · Payment Tracking**

![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Language](https://img.shields.io/badge/Language-Java%2011-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Min SDK](https://img.shields.io/badge/Min%20SDK-24%20(Android%207.0)-blue?style=for-the-badge)
![Target SDK](https://img.shields.io/badge/Target%20SDK-35-blueviolet?style=for-the-badge)
![Database](https://img.shields.io/badge/Storage-SQLite%20%7C%20Firebase-FFA000?style=for-the-badge&logo=firebase&logoColor=white)
![Status](https://img.shields.io/badge/Status-Complete-success?style=for-the-badge)

</div>

---

## 📖 Overview

**ScholarTrack** is an Android application designed to help engineering students and scholarship coordinators track MahaDBT scholarship applications—from checking scheme eligibility to monitoring approval and payment.

The application organizes the process into clear steps: checking potentially eligible schemes, preparing required documents, completing the online application, obtaining coordinator verification, submitting a signed hard copy to the college scholarship section, and tracking progress toward approval and payment.

ScholarTrack is a **manual tracking companion**. It does not fetch application status directly from the official MahaDBT portal, and its scheme information should be treated as reference data.

## ✨ Key Features

### 🎓 Student Features

- **🔎 Scheme Eligibility Checker**
  - Filters reference scholarship schemes using category, income, gender, and special conditions.
  - Displays schemes that may match the details entered by the student.
- **📊 Eight-Stage Application Tracker**
  - Tracks progress from eligibility checks through the application process to credited payment.
  - Records stage progress and related notes.
- **📄 Document Checklist**
  - Tracks required documents and their status locally using SQLite.
  - Supports tracking document types such as Xerox copies and originals.
- **📞 Coordinator & College Contacts**
  - Saves contact details for the class coordinator, department coordinator, and scholarship section.
  - Provides one-tap calling and SMS actions through Android intents.
- **📝 Grievance Drafting**
  - Helps students draft issues related to their scholarship applications.

### 🛡️ Coordinator Features

- **🔐 Role-Based Authentication**
  - Students and coordinators register and log in using Firebase Authentication.
- **📋 Coordinator Dashboard**
  - Displays student application information for coordinator review.
- **✅ Verification Progress**
  - Allows coordinators to update application verification stages.
- **👤 Student Application Details**
  - Provides a detailed view of a student's tracked application.

---

## ⚡ Architecture & Data Flow

ScholarTrack combines locally stored reference data and checklists with cloud-synced user and application information.

```text
                    ┌─────────────────────────────┐
                    │       ScholarTrack UI        │
                    │ Activities, Fragments, Forms │
                    └──────────────┬──────────────┘
                                   │
              ┌────────────────────┼────────────────────┐
              ▼                    ▼                    ▼
    ┌──────────────────┐  ┌─────────────────┐  ┌────────────────────┐
    │ Static Scheme    │  │ Local SQLite    │  │ Firebase           │
    │ Data             │  │ Database        │  │ Authentication     │
    │ assets/schemes   │  │ Documents and   │  │ + Realtime Database│
    │ .json            │  │ drafted issues  │  │ Profiles/stages    │
    └──────────────────┘  └─────────────────┘  └────────────────────┘
```

### Data Flow

1. **Static reference data:** Scholarship schemes and common document requirements are loaded from `assets/schemes.json`.
2. **Local storage:** Document checklist data and drafted grievances are stored using `DbHelper.java` and SQLite.
3. **Cloud data:** User profiles, application progress, and saved contacts are synchronized through Firebase Realtime Database using `FirebaseRepository.java`.
4. **Authentication:** Firebase Authentication handles user registration and login.

## 🗄️ Firebase Realtime Database Structure

The following is a simplified representation of the database structure:

### Firebase Database Structure
```json
{
  "users": {
    "uid": { "name": "...", "email": "...", "role": "...", "rollNo": "..." }
  },
  "applications": {
    "uid": {
      "schemeId": "...",
      "applicationId": "...",
      "currentStage": 3,
      "stages": {
        "1": { "done": true, "date": "...", "note": "...", "updatedBy": "..." }
      }
    }
  },
  "contacts": {
    "uid": {
      "classCoordinator": { "name": "...", "phone": "..." },
      "deptCoordinator": { "name": "...", "phone": "..." },
      "scholarshipSection": { "name": "...", "phone": "..." }
    }
  }
}
```

---

## 🧰 Tech Stack

| Category | Technology |
| :--- | :--- |
| **Platform** | Android |
| **Language** | Java 11 |
| **UI** | XML layouts, Material Components, ConstraintLayout |
| **Authentication** | Firebase Authentication |
| **Cloud Database** | Firebase Realtime Database |
| **Local Database** | SQLite |
| **Preferences** | SharedPreferences |
| **Static Reference Data** | JSON asset file (`assets/schemes.json`) |
| **Minimum SDK** | API 24 — Android 7.0 |
| **Target SDK** | API 35 |
| **Core Dependencies** | `appcompat`, `material`, `activity`, `constraintlayout`, `firebase-bom`, `core-splashscreen:1.0.1` |

> **API note:** ScholarTrack does not use an external REST API to retrieve MahaDBT application status. Scheme reference data is loaded locally, while application-related user data is synchronized through Firebase.

## 🧭 Screens & Activities

| Screen / Feature | Class Name |
| :--- | :--- |
| Splash Screen | `SplashActivity` |
| User Login | `LoginActivity` |
| Role-Based Registration | `RegisterActivity` |
| Student Dashboard | `StudentHomeActivity` |
| Student Dashboard Fragments | `HomeFragment`, `TrackerFragment`, `DocumentFragment`, `HelpFragment` |
| Coordinator Dashboard | `CoordinatorHomeActivity` |
| Student Application Details | `StudentDetailActivity` |
| Eligibility Input Form | `EligibilityActivity` |
| Eligible Schemes List | `SchemeResultsActivity` |
| Issue / Grievance Drafting | `GrievanceActivity` |
| Contact Directory | `ContactsActivity` |

## 📂 Project Structure

The following is the high-level package organization described for ScholarTrack. Actual filenames may vary by project implementation.

```text
ScholarTrack/
├── app/
│   └── src/main/
│       ├── assets/
│       │   └── schemes.json
│       ├── java/com/example/scholartrack/
│       │   ├── data/
│       │   │   ├── models/
│       │   │   ├── FirebaseRepository.java
│       │   │   └── DbHelper.java
│       │   ├── ui/
│       │   │   ├── student/
│       │   │   └── coordinator/
│       │   └── util/
│       │       └── Constants.java
│       └── res/
│           ├── layout/
│           ├── drawable/
│           └── values/
└── README.md
```

---

## 🚀 Getting Started

### Prerequisites

- Android Studio (Ladybug or newer recommended)
- Java 11
- Android emulator or physical Android device running Android 7.0 (API 24) or later
- A Firebase project

### Installation

1. **Clone the repository**

   ```bash
   git clone <YOUR_SCHOLARTRACK_REPOSITORY_URL>
   cd ScholarTrack
   ```

   Replace the placeholder URL with the actual repository URL.

2. **Open the project**

   - Open Android Studio.
   - Select **Open** and choose the `ScholarTrack` project directory.
   - Allow Gradle to sync.

3. **Configure Firebase**

   - Create a project in the [Firebase Console](https://console.firebase.google.com/).
   - Register an Android app with package name `com.example.scholartrack`.
   - Download `google-services.json` and place it in the `app/` directory.
   - Ensure the Firebase configuration file is excluded from public version control when appropriate.
   - Enable **Email/Password** under Firebase Authentication.
   - Create a Firebase Realtime Database and configure rules appropriate to your development or production environment.

   **Security warning:** Do not deploy an open database rule such as unrestricted read/write access to production. Restrict access by authenticated user and role, and validate authorization for coordinator-only operations.

4. **Run the app**

   - Connect a device with USB debugging enabled or start an emulator.
   - Select the app configuration and click **Run** in Android Studio.

## 🧪 Test Accounts

Create test users from the app's Registration screen:

- **Student:** Select the `Student` role and complete the registration form.
- **Coordinator:** Select the `Coordinator` role and enter the coordinator access code defined by `Constants.COORDINATOR_CODE` in `Constants.java`.

Use only a development/test access code for demonstrations. A hard-coded coordinator code is not a strong production authorization mechanism; coordinator permissions should be enforced by trusted backend rules or server-side logic.

## ⚠️ Important Notes & Limitations

- **Reference scheme data:** Eligibility results are indicative only. Confirm current scheme conditions, required documents, deadlines, and final eligibility on the official [MahaDBT portal](https://mahadbt.maharashtra.gov.in/).
- **Manual status tracking:** ScholarTrack does not read official application status directly from MahaDBT. Students and coordinators must update tracked progress in the app.
- **Sensitive data:** The stated implementation does not store Aadhaar numbers, bank account numbers, or scanned certificate images.
- **Privacy and access control:** Configure Firebase Authentication and database rules carefully. Do not store sensitive personal information unless there is a clear need and appropriate security controls.
- **Affiliation:** ScholarTrack is a college competition project and is **not affiliated with or endorsed by the Government of Maharashtra**.

## 🛣️ Roadmap

- [ ] Explore permitted options for automated application-status updates from MahaDBT.
- [ ] Add certificate scanning and document uploading.
- [ ] Add secure storage for Aadhaar and bank account details only if required and after implementing appropriate security and privacy safeguards.
- [ ] Support multiple academic years.
- [ ] Add push notifications for college-wide announcements.

## 👩‍💻 Author

**Yashashri Santosh Kawalkar**  
CSE (AIML)  
P. R. Pote Patil College of Engineering & Management, Amravati

---

## 📄 Project Status

**Status: Complete**

The current implementation supports role-based authentication, scheme eligibility filtering, application-stage tracking, local document checklists, coordinator progress updates, contact shortcuts, and grievance drafting. Items listed in the roadmap are planned enhancements and are not represented as implemented features.
