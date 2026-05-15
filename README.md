# Grameen-Light Energy Efficiency & Streetlight Audit

A local-first Android application for citizens to report streetlight issues and track energy efficiency. Built with Kotlin, Jetpack Compose, and Room Database following MVVM architecture.

## 🌟 Features

### Main Screen (Map View)
- **Interactive Pole Grid**: Display streetlight poles in a 2-column LazyVerticalGrid layout
- **Color-Coded Status Indicators**:
  - 🟢 Green: Working poles
  - 🔴 Red: Fused poles
  - 🟠 Orange: Poles burning during daytime
- **Quick Reporting**: Tap any pole to open the reporting bottom sheet
- **Real-time Status Updates**: Pole status updates immediately after reporting

### Reporting Flow
- **ModalBottomSheet Interface**: Clean, intuitive reporting interface
- **Two Report Types**:
  - "Report Fused" - For non-functional streetlights
  - "Report Burning in Day" - For energy-wasting daytime operation
- **Automatic Complaint Generation**: Unique UUID-based complaint IDs
- **Instant Status Updates**: Pole status updates in Room database upon submission

### Dashboard
- **Active Complaints Tracking**: View all pending complaints with details
- **Energy Impact Calculator**: 
  - Formula: Total Daytime Reports × 0.5 kWh
  - Visual energy savings card with impact metrics
- **Complaint Details**:
  - Issue type and status (PENDING/FIXED)
  - Pole ID and Complaint ID
  - Formatted timestamp
- **Empty State Handling**: Graceful display when no active complaints exist

## 🏗️ Architecture

### MVVM Pattern
- **Model**: Room entities (StreetlightPole, Complaint) with TypeConverters for enums
- **View**: Jetpack Compose screens with Material 3 design
- **ViewModel**: Sealed UI state classes (Loading, Success, Error) with Factory pattern
- **Repository**: Clean separation of concerns with Dispatchers.IO for thread safety

### Data Layer
- **Room Database**: Local-first persistence with pre-populated mock data
- **15 Mock Poles**: Automatically inserted on first launch via Database Callback
- **Flow-Based Reactive Updates**: Real-time UI synchronization
- **TypeConverters**: Proper enum handling for PoleStatus and ComplaintStatus

## 🛠️ Technologies Used

### Core Technologies
- **Kotlin** - Programming language
- **Jetpack Compose** - Modern UI toolkit
- **Material 3** - Design system

### Architecture & Data
- **MVVM Architecture** - Model-View-ViewModel pattern
- **Repository Pattern** - Data layer abstraction
- **Room Database** - Local persistence (SQLite)
- **TypeConverters** - Enum handling for Room

### Concurrency & State Management
- **Kotlin Coroutines** - Asynchronous programming
- **Flow** - Reactive data streams
- **StateFlow** - State management
- **collectAsStateWithLifecycle()** - Lifecycle-aware state collection

### Navigation
- **Jetpack Navigation Compose** - Screen navigation

### Dependency Management
- **Gradle (Kotlin DSL)** - Build system
- **kapt** - Annotation processing for Room

### Android SDK
- **minSdk 28** (Android 9.0 Pie)
- **targetSdk 36**
- **compileSdk 36**

## 📱 Data Models

### StreetlightPole
```kotlin
- id: String (Primary Key)
- status: PoleStatus (Enum: WORKING, FUSED, DAYTIME_ON)
- lat: Double (Latitude)
- lng: Double (Longitude)
- lastUpdated: Long (Timestamp)
