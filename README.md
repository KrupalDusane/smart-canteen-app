# 🍽️ NMIMS Canteen App

An Android application designed to streamline the canteen ordering experience for NMIMS students and staff.
The app provides real-time order tracking, smart scheduling, and an efficient admin management system.

---

## 🚀 Features

### 👨‍🎓 For Students

* 📋 **Food Ordering** – Browse menu and place orders easily
* ⏰ **Scheduled Pickups** – Select convenient pickup time
* 📦 **Order Tracking** – Real-time status updates (Pending → Preparing → Ready → Completed)
* 📜 **Order History** – View past and current orders
* 🔥 **Hot Selling Items** – Discover trending food items
* 🔔 **Notifications** – Get notified when order status changes

---

### 🧑‍💼 For Admin

* 📊 **Dashboard Overview** – View revenue, total orders, pending orders
* 📈 **Analytics** – Track daily & monthly revenue and top-selling items
* 🧾 **Order Management** – Accept, reject, prepare, and mark orders as ready
* 🍴 **Menu Management** – Add, update, and manage food items
* ⚡ **Real-time Updates** – Orders reflect instantly on admin panel

---

## 🛠️ Tech Stack

* **Language:** Java
* **Frontend:** XML (Material Design UI)
* **Backend:** Firebase Firestore
* **Authentication:** Firebase Auth
* **Architecture:** Activity-based structure with adapters and model classes

---

## ⚙️ Setup Instructions

### 🔹 Prerequisites

* Android Studio (Ladybug or newer)
* Firebase project

---

### 🔹 Steps

1. Clone the repository:

```bash
git clone https://github.com/YOUR_USERNAME/NmimsCanteenApp.git
```

2. Open in Android Studio

3. Firebase Setup:

* Go to Firebase Console
* Create a new project
* Add Android app with package:

  ```
  com.example.nmimscanteenapp
  ```
* Download `google-services.json`
* Place it inside:

  ```
  app/
  ```

4. Sync Gradle and Run the app

---

## 🔄 Order Flow (Core Logic)

1. Student places order → stored in Firestore
2. Admin receives order in real-time
3. Admin updates status:

   * Pending → Accepted → Preparing → Ready → Completed
4. Student receives live updates and notifications

---

## 🔒 Security

* `google-services.json` is excluded using `.gitignore`
* No sensitive data is hardcoded
* Keystore files are not uploaded
* Safe Firebase integration followed

---

## 💡 Problem Solved

Traditional canteen systems involve:

* Long queues
* No order tracking
* Inefficient communication

This app solves it by:
✔ Digital ordering
✔ Real-time tracking
✔ Reduced waiting time
✔ Better admin control

---

## 📌 Future Enhancements

* Online payment integration (UPI/Stripe)
* AI-based food recommendations
* Token-based queue system
* Multi-canteen support

---

## 📄 License

This project is developed for educational purposes.
