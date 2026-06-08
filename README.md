# Mem Info

A comprehensive Android application designed to help pastors and community leaders manage families, track household members, remember important dates like birthdays and anniversaries, and record periodic house visit logs. Built with modern Android development practices using Kotlin and Jetpack Compose.

## Key Features

- **Family Management:** Group members logically into relational household units. Easily link individuals directly to a family and define their roles (Head, Spouse, Child, etc.).
- **Member Profiles & Contacts:** Store important member details such as date of birth, wedding dates, address, and phone numbers. Includes quick-actions for calling or mapping addresses directly.
- **Visit Logs Tracker:** Keep a detailed history of personal visits, tracking the date, reason for the visit, prayer requests, and custom notes. 
- **Upcoming Events Dashboard:** A clean summary reflecting the next 30 days of birthdays and anniversaries, ensuring important dates are never missed.
- **Backup & Core Portability:** Easily export the entire directory database as a portable JSON file, and import it later to restore all state and records.

## Tech Stack

- **UI:** Jetpack Compose (Material Design 3)
- **Language:** Kotlin
- **Architecture:** MVVM Design Pattern with `StateFlow`
- **Data Persistence:** Local SQLite database (Room) with Background Sync Support

## Getting Started

1. Set up your development environment with Android Studio.
2. Clone or download the repository.
3. Sync project with Gradle files.
4. Run the configuration on an Android device or emulator. 

## Best Practices

- Requires `POST_NOTIFICATIONS` for background birthday reminders (Android 13+).
- Secure backup implementation supports full data migration seamlessly via structured JSON.
