# How to Run It

1. **Build the project:**
   - Open a terminal in the project root.
   - Run:
     ```powershell
  ./mvnw -q -DskipTests package
     ```

2. **Start the application:**
   - Run:
     ```powershell
  ./mvnw spring-boot:run
     ```


3. **Access the app in your browser:**

   Open your web browser and use the following URLs to explore the application:

   - **Demo Store:** [http://localhost:8080/](http://localhost:8080/)
     - This is a sample storefront where you can test the payment gateway as a customer. Click "Buy Now" to open the checkout widget and simulate a payment using the test card details shown on the page.

   - **Merchant Registration:** [http://localhost:8080/register](http://localhost:8080/register)
     - Use this page to create a new merchant account. Enter your name, email, and password. After registering, you can log in as a merchant.

   - **Merchant Login:** [http://localhost:8080/login](http://localhost:8080/login)
     - Log in with your merchant email and password. You can use the demo merchant (see below) or your own registered account.

   - **Portal Dashboard:** [http://localhost:8080/portal/dashboard](http://localhost:8080/portal/dashboard)
     - After logging in, this page shows all your payment transactions. You can review transaction status, amounts, and details.

   - **Portal Settings:** [http://localhost:8080/portal/settings](http://localhost:8080/portal/settings)
     - Here you can view and copy your API key (needed for API calls and SDK integration) and set a webhook URL to receive payment notifications. This is also where you can log out or return to the dashboard.

   - **H2 Database Console:** [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
     - This is a web interface for the in-memory H2 database. Use it for debugging or inspecting data. The JDBC URL is `jdbc:h2:mem:payflow`, user is `sa`, and the password is `123`.

**Demo Merchant Account:**

On startup, a demo merchant is created automatically for you to use:

- Email: `demo@merchant.test`
- Password: `demo1234`

You can log in with this account to access the portal, copy the API key, and test the full merchant experience without registering a new account.

---

# What Was Built

This project is a full-stack payment gateway demo called **PayFlow**. It includes:

- A RESTful Payment API (Java/Spring Boot)
- Merchant portal (register, login, dashboard, settings)
- JavaScript SDK for embedding a checkout widget
- Demo storefront page for testing the payment flow
- Webhook support for merchant integration

**Key features:**
- Secure merchant registration and login
- API key authentication for payments
- Simulated payment processing with clear success/failure rules
- Transaction dashboard for merchants
- Webhook notifications for payment events
- H2 in-memory database for easy setup
- Clean, modern UI with Thymeleaf and static assets

---

# Key Design Decisions

**1. Spring Boot & Java 21:**
  - Chosen for rapid development, strong ecosystem, and modern language features.

**2. H2 In-Memory Database:**
  - Enables zero-config, fast resets for demo/testing. No external DB needed.

**3. API Key Authentication:**
  - Merchants use API keys for secure, stateless access to payment endpoints.

**4. Webhook Support:**
  - Merchants can receive real-time payment notifications for integration with their own systems.

**5. JavaScript SDK Widget:**
  - Provides a simple way for merchants to embed the checkout experience in any site.

**6. Clear Separation of Concerns:**
  - Controllers handle HTTP, services handle business logic, repositories handle data.

**7. Security:**
  - Spring Security for portal login; API key for REST endpoints; CSRF enabled for forms.

**8. Demo Merchant Seeding:**
  - A demo merchant is auto-created for easy testing and demonstration.

**9. Minimal Data Retention:**
  - Only card last4 is stored; no sensitive card data is persisted.

**10. Asynchronous Webhooks:**
  - Webhook delivery is non-blocking to ensure fast payment response times.

---
