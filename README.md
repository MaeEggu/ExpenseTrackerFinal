# Expense Tracker

Expense Tracker is a beginner-friendly JavaFX desktop application built with FXML, CSS, Maven, and a clean OOP package structure. It lets users add, view, edit, and delete expenses while converting supported currencies to PHP for totals and summaries.

The project now stores expenses permanently in a Supabase PostgreSQL database using JDBC and `PreparedStatement`.

## Project Details

- IDE: IntelliJ IDEA
- Java: 25.0.3
- GUI: JavaFX 25
- UI files: FXML, compatible with Scene Builder
- Styling: External CSS
- Build system: Maven
- Database: Supabase PostgreSQL

## Features

- Home page with navigation
- Add expense form with validation
- Currency dropdown: PHP, USD, EUR, JPY
- Category dropdown: Food, Transportation, School, Bills, Shopping, Entertainment, Others
- TableView expense list loaded from Supabase
- PHP conversion column
- Total expense summary
- Category totals
- PieChart category graph
- Edit selected expense
- Delete selected expense
- Delete all expenses

## Folder Structure

```text
ExpenseTrackerFinal
â”œâ”€â”€ pom.xml
â”œâ”€â”€ README.md
â”œâ”€â”€ .gitignore
â”œâ”€â”€ .env.example
â”œâ”€â”€ config
â”‚   â””â”€â”€ database.properties.example
â”œâ”€â”€ database
â”‚   â””â”€â”€ schema.sql
â””â”€â”€ src
    â””â”€â”€ main
        â”œâ”€â”€ java
        â”‚   â”œâ”€â”€ application
        â”‚   â”‚   â””â”€â”€ MainApp.java
        â”‚   â”œâ”€â”€ controller
        â”‚   â”‚   â”œâ”€â”€ AddExpenseController.java
        â”‚   â”‚   â”œâ”€â”€ EditExpenseController.java
        â”‚   â”‚   â”œâ”€â”€ HomeController.java
        â”‚   â”‚   â”œâ”€â”€ NavigationController.java
        â”‚   â”‚   â””â”€â”€ ViewExpenseController.java
        â”‚   â”œâ”€â”€ dao
        â”‚   â”‚   â””â”€â”€ ExpenseDAO.java
        â”‚   â”œâ”€â”€ database
        â”‚   â”‚   â”œâ”€â”€ DatabaseConnection.java
        â”‚   â”‚   â”œâ”€â”€ DatabaseConnectionTest.java
        â”‚   â”‚   â”œâ”€â”€ DatabaseException.java
        â”‚   â”‚   â””â”€â”€ DatabaseSchemaSetup.java
        â”‚   â”œâ”€â”€ model
        â”‚   â”‚   â””â”€â”€ Expense.java
        â”‚   â”œâ”€â”€ repository
        â”‚   â”‚   â””â”€â”€ ExpenseRepository.java
        â”‚   â”œâ”€â”€ service
        â”‚   â”‚   â””â”€â”€ ExpenseService.java
        â”‚   â”œâ”€â”€ utility
        â”‚   â”‚   â”œâ”€â”€ Converter.java
        â”‚   â”‚   â”œâ”€â”€ CurrencyConverter.java
        â”‚   â”‚   â””â”€â”€ ExpenseValidator.java
        â”‚   â””â”€â”€ module-info.java
        â””â”€â”€ resources
            â”œâ”€â”€ css
            â”‚   â””â”€â”€ style.css
            â””â”€â”€ fxml
                â”œâ”€â”€ add_expense.fxml
                â”œâ”€â”€ edit_expense.fxml
                â”œâ”€â”€ home.fxml
                â””â”€â”€ view_expense.fxml
```

## OOP Structure

- `Expense` is the model class that encapsulates expense data.
- `ExpenseDAO` defines the database operations the app needs.
- `ExpenseRepository` implements the DAO using JDBC and safe `PreparedStatement` queries.
- `DatabaseConnection` creates Supabase PostgreSQL connections from environment variables, `.env`, or the legacy local config file.
- `ExpenseService` manages business logic, currency conversion, summaries, and database calls.
- Controllers handle JavaFX events and delegate logic to `ExpenseService`.
- FXML files define the UI layout, while `style.css` controls the visual design.

## Supabase Setup Guide

### 1. Create a Supabase Account

1. Go to `https://supabase.com`.
2. Click `Start your project` or `Sign in`.
3. Create an account using GitHub, Google, or email.
4. After logging in, open the Supabase Dashboard.

### 2. Create a New Project

1. Click `New project`.
2. Choose or create an organization.
3. Enter a project name, for example `expense-tracker`.
4. Enter a database password and save it somewhere private. The Java app needs this password.
5. Choose a region close to you.
6. Click `Create new project`.
7. Wait for Supabase to finish creating the project.

### 3. Create the Expenses Table

1. Open your Supabase project.
2. Go to `SQL Editor`.
3. Open this local project file:

```text
database/schema.sql
```

4. Copy the full SQL script.
5. Paste it into Supabase SQL Editor.
6. Click `Run`.

The important table created by the script is:

```sql
create table if not exists public.expenses (
    id bigint generated by default as identity primary key,
    item_name text not null,
    category text not null,
    currency text not null,
    amount numeric(12, 2) not null check (amount > 0),
    converted_php_value numeric(12, 2) not null check (converted_php_value >= 0),
    date_added date not null default current_date,
    created_at timestamp with time zone not null default now()
);
```

### 4. Find Your Supabase Project URL and API Key

Your current Project URL format is:

```text
https://qypcjqytkeclgzmbhvqp.supabase.co
```

To find it yourself:

1. In Supabase, go to `Project Settings`.
2. Open `API`.
3. Copy `Project URL`.

To find an API key:

1. In the same `API` page, find `Project API keys`.
2. Copy the `anon public` key only if you later build a REST API version of this app.

Important: this JavaFX project uses PostgreSQL JDBC, so it does not need the Supabase API key. Do not paste API keys into this project. If a private key was shared publicly or uploaded anywhere, rotate it in Supabase.

### 5. Get the JDBC Database Connection Values

JDBC does not use the Project URL. It needs the PostgreSQL connection information.

1. In your Supabase dashboard, click `Connect`.
2. Choose a Postgres connection string.
3. For most desktop apps and home networks, choose `Session pooler` because it supports IPv4.
4. Copy the host, user, password, and database name.

Example Supabase pooler string:

```text
postgres://postgres.qypcjqytkeclgzmbhvqp:YOUR_DATABASE_PASSWORD@aws-0-your-region.pooler.supabase.com:5432/postgres
```

Convert it to this Java JDBC format:

```text
jdbc:postgresql://aws-0-your-region.pooler.supabase.com:5432/postgres?sslmode=require
```

For your project reference, the database user is usually:

```text
postgres.qypcjqytkeclgzmbhvqp
```

The password is the database password from Supabase, not the Project URL, not the API key, and not your Supabase account login password.

### 6. Secure Database Credentials with `.env`

The safest beginner-friendly setup is to keep your Supabase credentials in a local `.env` file. This file is ignored by Git, so it will not be uploaded to GitHub.

Copy this file:

```text
.env.example
```

Paste it in the project root as:

```text
.env
```

Then edit `.env`:

```env
SUPABASE_DB_URL=jdbc:postgresql://aws-0-your-region.pooler.supabase.com:5432/postgres?sslmode=require
SUPABASE_DB_USER=postgres.qypcjqytkeclgzmbhvqp
SUPABASE_DB_PASSWORD=your_database_password
```

The app checks credentials in this order:

1. Computer environment variables
2. Local `.env` file
3. Legacy `config/database.properties`

Do not put your Supabase API key in `.env`. This JavaFX desktop app uses JDBC, so it needs the PostgreSQL database password.

Legacy option: `config/database.properties` still works if you prefer it, and it is also ignored by Git.

You can also use environment variables instead:

```text
SUPABASE_DB_URL
SUPABASE_DB_USER
SUPABASE_DB_PASSWORD
```

Environment variables are useful when running the app on another computer or in a deployment environment.

### 7. Test Database Connectivity

In IntelliJ IDEA:

1. Open IntelliJ IDEA.
2. Click `File > Open`.
3. Select the project folder:

```text
C:\ExpenseTracker\ExpenseTrackerFinal
```

4. IntelliJ should detect `pom.xml` and import the project as Maven.
5. Wait for Maven to download the JavaFX and PostgreSQL dependencies.
6. Create `.env` using the steps above.
7. Open:

```text
src/main/java/database/DatabaseConnectionTest.java
```

8. Click the green Run button beside the `main` method.

If `.env` is not detected, open `Run > Edit Configurations` and set:

```text
Working directory: C:\ExpenseTracker\ExpenseTrackerFinal
```

Expected output:

```text
Connected to Supabase PostgreSQL.
Database: postgres
User: postgres.qypcjqytkeclgzmbhvqp
```

If the test fails, check:

- `.env` exists in the project root.
- `SUPABASE_DB_URL` starts with `jdbc:postgresql://`.
- `SUPABASE_DB_USER` starts with `postgres.` and includes your project reference.
- `SUPABASE_DB_PASSWORD` is the database password, not an API key.
- Your Supabase project is active.
- You used the session pooler if your network does not support IPv6.

### 8. Run the JavaFX App with Supabase

From the project folder:

```bash
mvn clean javafx:run
```

Or in IntelliJ IDEA:

1. Open `C:\ExpenseTracker\ExpenseTrackerFinal` in IntelliJ.
2. Wait until Maven indexing/import finishes.
3. Open:

```text
src/main/java/application/MainApp.java
```

4. Click the green Run button beside the `main` method.
5. If the app cannot find `.env`, go to `Run > Edit Configurations` and set:

```text
Working directory: C:\ExpenseTracker\ExpenseTrackerFinal
```

To confirm Supabase storage works:

1. Add an expense in the app.
2. Open `View Expense`.
3. Confirm the expense appears in the table.
4. Close and reopen the app.
5. Open `View Expense` again. The expense should still be there.

## Sample SQL Queries

Run these in Supabase SQL Editor to inspect your saved data:

```sql
select * from public.expenses order by date_added desc, id desc;
```

```sql
select category, sum(converted_php_value) as total_php
from public.expenses
group by category
order by total_php desc;
```

```sql
select sum(converted_php_value) as grand_total_php
from public.expenses;
```

## Maven Dependencies

The important dependencies are already configured in `pom.xml`:

- `javafx-controls`
- `javafx-fxml`
- `postgresql`

No local JavaFX SDK path is required. This makes the project portable for GitHub and easy to import into IntelliJ IDEA.

## Scene Builder

FXML files are stored in:

```text
src/main/resources/fxml
```

Open any FXML file in Scene Builder to edit the UI visually. Controller references are already configured with `fx:controller`, and the shared stylesheet is linked from each FXML file.

## Live Currency Rates

The app uses the free Frankfurter exchange-rate API for latest currency conversion rates:

```text
https://api.frankfurter.dev/v1/latest?base=PHP&symbols=USD,EUR,JPY
```

No API key is required. If the app cannot reach the API, it automatically falls back to the manual rates in `CurrencyConverter.java` so adding expenses still works offline.
## Currency Conversion

The application stores the original currency and amount, then calculates a PHP value for totals and summaries. Conversion rates are centralized in:

```text
src/main/java/utility/CurrencyConverter.java
```

Update the rates there when you want to change conversion values.

## Notes for GitHub Upload

- Upload `pom.xml`, `src`, `database/schema.sql`, `config/database.properties.example`, and `README.md`.
- Upload `.env.example`.
- Do not upload `.env`.
- Do not upload `config/database.properties`.
- Do not upload database passwords or Supabase API keys.
- After cloning from GitHub, each user should create their own `.env`.
