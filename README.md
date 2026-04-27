# NMIMS Canteen App 🍽️

An Android application designed to streamline the canteen ordering process for NMIMS students and staff. The app features a robust ordering system, real-time status updates, and administrative analytics.

## 🚀 Features

### For Students:
- **Food Ordering:** Browse the canteen menu and place orders.
- **Scheduled Pickups:** Select a convenient time to collect your food.
- **Order History:** Track current and past orders.
- **Hot Selling Items:** See what's trending in the canteen.
- **Notifications:** Get notified when your order is ready.

### For Admin:
- **Dashboard Overview:** Monitor revenue, total orders, and pending requests.
- **Analytics:** View top-performing items by quantity and revenue.
- **Order Management:** Accept, prepare, or reject orders with status updates.
- **Menu Management:** Add, edit, or toggle availability of menu items.

## 🛠️ Tech Stack
- **Language:** Java
- **Database:** Firebase Firestore
- **Authentication:** Firebase Auth
- **UI:** XML with Material Design

## ⚙️ Setup Instructions

### Prerequisites
- Android Studio Ladybug or newer.
- A Firebase project.

### Local Setup
1. Clone the repository:
   ```bash
   git clone https://github.com/YOUR_USERNAME/NmimsCanteenApp.git
   ```
2. **Firebase Configuration:**
   - Go to the [Firebase Console](https://console.firebase.google.com/).
   - Add an Android app with the package name `com.example.nmimscanteenapp`.
   - Download the `google-services.json` file.
   - Place `google-services.json` in the `app/` directory of the project.
3. Build and run the app in Android Studio.

> **Note:** The `google-services.json` file and signing keystores are excluded from this repository for security reasons. You must provide your own Firebase configuration to run the app.

## 🔒 Security
- `google-services.json` is ignored via `.gitignore`.
- No sensitive keys or passwords are hardcoded in the source code.
- Sanitized payment placeholders are used for public display.

## 📄 License
This project is for educational purposes.
