# ScholarTrack


**ScholarTrack** is an Android application that helps engineering students and scholarship coordinators track MahaDBT scholarship applications, from eligibility to payment. It covers the full process: checking eligible schemes, collecting documents, coordinator verification, the signed hard copy, submission to the college scholarship section, and approval.
**Status**: Complete.

## The Problem It Solves

The MahaDBT scholarship application process involves many manual steps: gathering documents, filling out the MahaDBT online form, showing the form to class and department coordinators, submitting a signed hard copy to the college scholarship section, and waiting for approval and payment. Students often miss deadlines, forget which documents are required for their category, or lose track of their application's current stage. Coordinators struggle to track the progress of hundreds of students. ScholarTrack digitizes this workflow by providing step-by-step tracking, eligibility checks, and direct communication channels.

## Features

### Implemented
* **Role-Based Authentication**: Register and login as a Student or a Coordinator using Firebase Auth.
* **Scheme Eligibility Checker**: Filters available scholarships based on category, income, gender, and special conditions.
* **Application Tracker**: An 8-stage tracker tracking the process from eligibility to credited payment.
* **Document Checklist**: Local SQLite database to track the status of required documents (e.g., Xerox vs Original).
* **Coordinator Dashboard**: Coordinators can view student applications and update verification stages.
* **Direct Contacts**: Save contact details for coordinators and the scholarship section, with one-tap Intent to Call or SMS.
* **Grievance System**: Draft and send issues regarding applications.


### Planned
* Reading official application status directly from the MahaDBT portal.
* Document certificate scanning and uploading.
* Storing Aadhaar and bank account numbers securely.

## Screens

| Screen / Feature | Class Name |
| :--- | :--- |
| Splash Screen | `SplashActivity` |
| User Login | `LoginActivity` |
| Role-based Registration | `RegisterActivity` |
| Student Dashboard | `StudentHomeActivity` (Hosts `HomeFragment`, `TrackerFragment`, `DocumentFragment`, `HelpFragment`) |
| Coordinator Dashboard | `CoordinatorHomeActivity` |
| Student Application Details | `StudentDetailActivity` |
| Eligibility Input Form | `EligibilityActivity` |
| Eligible Schemes List | `SchemeResultsActivity` |
| Issue / Grievance Drafting | `GrievanceActivity` |
| Contact Directory | `ContactsActivity` |

## Tech Stack

| Component | Technology / Version |
| :--- | :--- |
| **Language** | Java 11 |
| **UI** | XML Layouts (Material Components, ConstraintLayout) |
| **Backend & Auth** | Firebase Realtime Database, Firebase Authentication |
| **Local Storage** | SQLite, SharedPreferences |
| **Minimum SDK** | API 24 (Android 7.0) |
| **Target SDK** | API 35 |
| **Libraries** | `appcompat`, `material`, `activity`, `constraintlayout`, `firebase-bom`, `core-splashscreen:1.0.1` |

*Note: The app does not use any external REST APIs. All reference scheme data is parsed locally from a JSON file, and user data is synced via Firebase.*


## Architecture

### Package Structure
* `com.example.scholartrack.data`: Contains models, Firebase repositories, and SQLite helpers.
* `com.example.scholartrack.ui`: Contains all Activities, Fragments, and Adapters separated by role (`student`, `coordinator`).
* `com.example.scholartrack.util`: Helper classes and constants.

### Data Flow
1. **Static Data**: Scholarship schemes and common document requirements are parsed into memory from `assets/schemes.json`.
2. **Local Data**: Document checklists and drafted grievances are stored in a local SQLite database (`DbHelper.java`).
3. **Cloud Data**: User profiles, application stage progress, and saved contacts are synced in real-time using Firebase Realtime Database via `FirebaseRepository.java`.

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

## Getting Started

### Requirements
* Android Studio (Ladybug or newer recommended).
* Java 11.
* An active Firebase Project.

### Setup Instructions
1. Clone the repository and open it in Android Studio.
2. **Firebase Setup**:
   * Create a project in the Firebase Console.
   * Add an Android app with the package name `com.example.scholartrack`.
   * Download the `google-services.json` file and place it in the `app/` directory. *(Note: `google-services.json` is intentionally excluded from the repository for security).*
   * Enable **Email/Password** sign-in under Authentication.
   * Create a **Realtime Database** and set the rules to allow authenticated users to read/write.
3. Sync Gradle and run the app on an emulator or physical device.

## Test Accounts

To test the application, launch the app and navigate to the Registration screen.
* **Student**: Select the "Student" role from the dropdown, fill in the details, and register.
* **Coordinator**: Select the "Coordinator" role. You will be prompted to enter a Coordinator Access Code. Use the hard-coded test code defined in `Constants.java` (`Constants.COORDINATOR_CODE`).

## Important Notes and Limitations

* **Reference Data Only**: The scheme data provided in the app is for reference purposes. Final eligibility and scheme details must always be confirmed on the official MahaDBT portal.
* **No API Integration**: The app cannot read the official application status from the MahaDBT servers. It is a manual tracking tool for students and college coordinators.
* **Privacy**: The app does **not** store sensitive information such as Aadhaar numbers, bank account numbers, or scanned certificate images. 
* **Affiliation**: This project was built for a college competition and is **not affiliated with or endorsed by the Government of Maharashtra**.


## Roadmap

- [ ] Integrate MahDBT portal scraping (if permitted) for automated status updates.
- [ ] Add support for multiple academic years.
- [ ] Implement push notifications for college-wide announcements.

## Author 

* **Author**: Yashashri Santosh Kawalkar, CSE(AIML), P. R. Pote Patil College of Engineering & Management, Amravati