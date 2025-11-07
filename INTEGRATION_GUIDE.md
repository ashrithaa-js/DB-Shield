# PostgreSQL Backup - Frontend/Backend Integration Guide

## 📋 Table of Contents
1. [Backend Architecture Flow](#backend-architecture-flow)
2. [Current Backend Components](#current-backend-components)
3. [Frontend Integration Points](#frontend-integration-points)
4. [Step-by-Step Implementation](#step-by-step-implementation)
5. [How to Start the Application](#how-to-start-the-application)

---

## 🔄 Backend Architecture Flow

### Current Backend Flow (Command-Line Mode)

```
┌─────────────────┐
│   Bootstrap     │  ← Entry Point (main method)
│   (main args)   │
└────────┬────────┘
         │
         ├─→ If args provided → HashHelper.encrypt() → Exit
         │
         └─→ Controller.getInstance().execute()
                    │
                    ▼
         ┌──────────────────┐
         │   Controller      │  ← Singleton Pattern
         │   (execute)       │
         └────────┬──────────┘
                  │
                  ▼
         ┌──────────────────┐
         │  BackupProcess    │  ← Main Backup Logic
         │   (execute)       │
         └────────┬──────────┘
                  │
                  ├─→ RegisterHelper (reads /etc/database_backup/register)
                  │   │
                  │   └─→ Parse each line: "host database user pass label type"
                  │
                  ├─→ For each Register:
                  │   │
                  │   ├─→ If type == "postgresql"
                  │   │   └─→ PostgreSQL.backup(host, user, pass, database, label)
                  │   │       │
                  │   │       ├─→ HashHelper.decrypt(pass)  ← Decrypt password
                  │   │       ├─→ Build pg_dump command
                  │   │       ├─→ Execute ProcessBuilder
                  │   │       └─→ Save to: Path.backups/label-database.DATE.backup
                  │   │
                  │   └─→ If type == "mysql"
                  │       └─→ MysqlSQL.backup(host, user, pass, database, label)
                  │           │
                  │           ├─→ HashHelper.decrypt(pass)  ← Decrypt password
                  │           ├─→ Build mysqldump command
                  │           ├─→ Execute ProcessBuilder
                  │           └─→ Save to: Path.backups/label-database.DATE.mysql.sql
                  │
                  └─→ SettingsHelper (reads /etc/database_backup/settings)
                      │
                      └─→ Properties: path.backups, path.pgdump
```

### Key Backend Components

#### 1. **Path Management** (`Path.java`)
- **Configuration Path**: `/etc/database_backup/` (Linux) or `C:/etc/database_backup/` (Windows)
- **Backup Path**: Read from `settings` file (default: `/opt/backups`)
- **Auto-creates directories if they don't exist**

#### 2. **Configuration Files**

**`/etc/database_backup/register`** - Database registrations
```
Format: host database user encrypted_pass label type
Example: 127.0.0.1 mydb postgres ENCRYPTED_PASS local_machine postgresql
```

**`/etc/database_backup/settings`** - System settings
```
path.backups=/opt/backups
path.pgdump=/usr/bin/pg_dump
```

#### 3. **Password Encryption**
- Uses `HashHelper.encrypt()` to encrypt passwords
- Uses `HashHelper.decrypt()` to decrypt passwords
- AES encryption with Base64 encoding

#### 4. **Backup Process**
- Reads all databases from `register` file
- For each database, calls appropriate backup tool
- Executes system commands (`pg_dump` or `mysqldump`)
- Saves backup files with timestamp

---

## 🎨 Frontend Integration Points

### Current Frontend Structure
```
MainApp.java (Application entry)
    ↓
Main.fxml (UI Layout)
    ↓
MainController.java (Event handlers - NEEDS INTEGRATION)
```

### What Needs to Be Done

#### ✅ **Option 1: Direct Integration (Recommended for GUI)**
Connect `MainController` directly to backup tools, bypassing file-based registration.

#### ✅ **Option 2: File-Based Integration**
Use existing `register` file system but add GUI for managing it.

---

## 🛠️ Step-by-Step Implementation

### **STEP 1: Update MainController to Connect with Backend**

The `MainController` needs to:
1. Collect form data (host, database, user, password, path, type)
2. Encrypt the password
3. Call the appropriate backup tool
4. Display progress/output in real-time
5. Handle errors gracefully

### **STEP 2: Add Missing Fields to FXML**

Current FXML is missing:
- **Host field** (defaults to localhost)
- **Database type selector** (PostgreSQL/MySQL)
- **Label field** (for backup file naming)

### **STEP 3: Implement Async Backup Execution**

Backup operations are blocking, so they need to run in a background thread to keep UI responsive.

### **STEP 4: Add Progress Feedback**

Show real-time output from backup process in the TextArea.

---

## 🚀 How to Start the Application

### **Method 1: Using JavaFX Application (GUI Mode)**

```bash
# Compile and run
mvn clean compile
mvn javafx:run

# OR directly
java --module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.fxml -cp target/classes com.fabriciojf.postgresql.backup.gui.MainApp
```

### **Method 2: Using Bootstrap (Command-Line Mode)**

```bash
# Run backup for all registered databases
java -jar database_backup.jar

# Encrypt a password
java -jar database_backup.jar mypassword
```

### **Method 3: Using Maven JavaFX Plugin**

```bash
mvn clean javafx:run
```

---

## 📝 Implementation Checklist

### Immediate Actions Required:

- [ ] **Update MainController.java**
  - [ ] Add host field handling
  - [ ] Add database type selection (PostgreSQL/MySQL)
  - [ ] Add label field handling
  - [ ] Implement password encryption
  - [ ] Connect to PostgreSQL.backup() or MysqlSQL.backup()
  - [ ] Add async execution (Task/Service)
  - [ ] Add real-time output streaming

- [ ] **Update Main.fxml**
  - [ ] Add Host input field
  - [ ] Add Database Type selector (ComboBox)
  - [ ] Add Label input field
  - [ ] Improve layout for new fields

- [ ] **Add JavaFX Dependencies to pom.xml**
  - [ ] Verify JavaFX modules are included
  - [ ] Add JavaFX runtime dependencies if needed

- [ ] **Handle Path Configuration**
  - [ ] Update Path.java for Windows compatibility
  - [ ] Allow user to set backup directory from GUI
  - [ ] Update SettingsHelper to save user preferences

- [ ] **Error Handling**
  - [ ] Add try-catch blocks
  - [ ] Show user-friendly error messages
  - [ ] Validate input fields

- [ ] **Testing**
  - [ ] Test PostgreSQL backup
  - [ ] Test MySQL backup
  - [ ] Test password encryption
  - [ ] Test error scenarios

---

## 🔧 Technical Details

### Backend Method Signatures

```java
// PostgreSQL Backup
PostgreSQL.backup(String host, String user, String encryptedPass, 
                  String database, String label)
    throws IOException, InterruptedException

// MySQL Backup  
MysqlSQL.backup(String host, String user, String encryptedPass, 
                String database, String label)
    throws IOException, InterruptedException

// Password Encryption
String encrypted = HashHelper.encrypt(String plainPassword);
String decrypted = HashHelper.decrypt(String encryptedPassword);
```

### Required Form Fields

1. **Host** - Database server address (e.g., "127.0.0.1" or "localhost")
2. **Database Name** - Name of the database to backup
3. **Username** - Database user
4. **Password** - Database password (will be encrypted)
5. **Backup Directory** - Where to save backup files
6. **Label** - Label for backup file naming (e.g., "production", "dev")
7. **Database Type** - "postgresql" or "mysql"

---

## 🎯 Next Steps

1. **Review this guide** - Understand the backend flow
2. **Update MainController** - Implement backend integration
3. **Update Main.fxml** - Add missing fields
4. **Test the integration** - Run backups through GUI
5. **Add enhancements** - Progress bars, log viewing, etc.

---

## 📚 Additional Resources

- **Backend Entry Point**: `Bootstrap.java`
- **Backend Controller**: `Controller.java`
- **Backup Logic**: `BackupProcess.java`
- **PostgreSQL Tool**: `PostgreSQL.java`
- **MySQL Tool**: `MysqlSQL.java`
- **Password Helper**: `HashHelper.java`
- **Path Management**: `Path.java`

---

**Ready to implement?** The next step is to update `MainController.java` to connect the frontend with the backend backup functionality!


