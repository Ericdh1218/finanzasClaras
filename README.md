# 💰 Gestor de Finanzas Personales (Personal Expense Tracker)

Aplicación móvil nativa para Android diseñada para el registro, categorización y control inteligente de finanzas personales. Permite monitorear ingresos, egresos y presupuestos mediante una interfaz modular y sincronización de datos en tiempo real respaldada por Firebase.

---

## 📱 Características Principales

* **Control de Transacciones:** Registro ágil de ingresos y egresos con categorización personalizable (alimentación, transporte, servicios, entretenimiento, etc.).
* **Navegación Modular por Fragments:** Arquitectura desacoplada para la visualización fluida del historial de movimientos, dashboards de balance y formularios de captura.
* **Persistencia y Sincronización en la Nube:** Integración con backend en la nube para resguardo de la información y disponibilidad multiplataforma en tiempo real.
* **Soporte Offline:** Manejo de caché local para permitir el registro de movimientos sin conexión y sincronización automática al restablecer el servicio.
* **Seguridad y Privacidad:** Autenticación de usuario para mantener el historial financiero estrictamente privado y protegido.

---

## 🛠️ Tecnologías y Herramientas

* **Lenguaje:** Java
* **IDE:** Android Studio
* **Gestor de Compilación:** Gradle (Groovy DSL)
* **Arquitectura & UI:** Android SDK, Fragments, View Binding, Material Design Components
* **Backend as a Service (BaaS):** 
  * Firebase Authentication (gestión de sesiones e identidad)
  * Firebase Cloud Firestore / Realtime Database (persistencia NoSQL y reactiva)
* **Control de Versiones:** Git & GitHub

---

## 📂 Arquitectura del Proyecto

El código está estructurado bajo principios de modularidad y separación de responsabilidades:

```text
app/src/main/java/com/tuusuario/finanzas/
│
├── data/
│   ├── models/          # Modelos de datos (Transaction, Category, User)
│   └── repository/      # Comunicación con los servicios de Firebase
│
├── ui/
│   ├── activities/      # Entry points principales (MainActivity, AuthActivity)
│   ├── fragments/       # Módulos de interfaz (DashboardFragment, HistoryFragment, AddExpenseFragment)
│   └── adapters/        # Adaptadores para RecyclerViews
│
└── utils/               # Formateadores de moneda, validaciones y constantes
