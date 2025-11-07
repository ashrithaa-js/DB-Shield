# Quick Start Guide - How to Start & Integrate

## 🚀 Quick Start Flow

### **1. Understanding the Current State**

#### Backend (Working):
- ✅ Reads databases from `/etc/database_backup/register` file
- ✅ Executes backups using `pg_dump` or `mysqldump`
- ✅ Encrypts/decrypts passwords
- ✅ Saves backups to configured directory

#### Frontend (Needs Integration):
- ✅ UI created (Main.fxml)
- ✅ Controller created (MainController.java)
- ❌ **NOT CONNECTED to backend yet**

---

## 📊 Complete Flow Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                    APPLICATION START                        │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
        ┌───────────────────────────────────┐
        │   Choose Mode:                    │
        │   1. GUI Mode (MainApp)           │
        │   2. CLI Mode (Bootstrap)         │
        └───────────────────────────────────┘
                            │
        ┌───────────────────┴───────────────────┐
        │                                       │
        ▼                                       ▼
┌───────────────┐                      ┌───────────────┐
│  GUI MODE     │                      │  CLI MODE     │
│  MainApp      │                      │  Bootstrap    │
└───────┬───────┘                      └───────┬───────┘
        │                                       │
        ▼                                       ▼
┌───────────────┐                      ┌───────────────┐
│ Main.fxml     │                      │ Read args     │
│ (UI Layout)   │                      │               │
└───────┬───────┘                      └───────┬───────┘
        │                                       │
        ▼                                       ▼
┌───────────────┐                      ┌───────────────┐
│MainController │                      │ Controller    │
│ (Events)      │                      │ .execute()    │
└───────┬───────┘                      └───────┬───────┘
        │                                       │
        │  [NEEDS INTEGRATION]                  │
        │                                       │
        └───────────────┬───────────────────────┘
                        │
                        ▼
            ┌───────────────────────┐
            │  BackupProcess        │
            │  .execute()           │
            └───────────┬───────────┘
                        │
        ┌───────────────┴───────────────┐
        │                               │
        ▼                               ▼
┌───────────────┐              ┌───────────────┐
│RegisterHelper │              │SettingsHelper │
│(Read register)│              │(Read settings)│
└───────┬───────┘              └───────┬───────┘
        │                               │
        ▼                               ▼
┌───────────────┐              ┌───────────────┐
│ Parse each    │              │ Get paths:    │
│ database line │              │ - backups dir │
│               │              │ - pg_dump     │
└───────┬───────┘              └───────────────┘
        │
        ▼
┌───────────────────────────────────┐
│ For each database:                │
│                                   │
│  ┌─────────────────────────────┐ │
│  │ If PostgreSQL:              │ │
│  │   PostgreSQL.backup()      │ │
│  │   - Decrypt password        │ │
│  │   - Run pg_dump command    │ │
│  │   - Save .backup file      │ │
│  └─────────────────────────────┘ │
│                                   │
│  ┌─────────────────────────────┐ │
│  │ If MySQL:                   │ │
│  │   MysqlSQL.backup()         │ │
│  │   - Decrypt password        │ │
│  │   - Run mysqldump command  │ │
│  │   - Save .sql file         │ │
│  └─────────────────────────────┘ │
└───────────────────────────────────┘
```

---

## 🎯 What You Need to Do

### **Step 1: Update MainController.java**

Replace the placeholder `onBackup()` method with actual backend integration:

```java
@FXML
private void onBackup(ActionEvent event) {
    // Get form values
    String host = hostField.getText().trim();  // NEEDS TO BE ADDED
    String dbName = dbNameField.getText().trim();
    String user = userField.getText().trim();
    String password = passwordField.getText();
    String backupPath = pathField.getText().trim();
    String label = labelField.getText().trim();  // NEEDS TO BE ADDED
    String dbType = dbTypeCombo.getValue();  // NEEDS TO BE ADDED
    
    // Validate
    if (host.isEmpty() || dbName.isEmpty() || user.isEmpty() || 
        password.isEmpty() || backupPath.isEmpty() || label.isEmpty()) {
        outputArea.setText("Please fill in all required fields.");
        return;
    }
    
    // Encrypt password
    String encryptedPass = HashHelper.encrypt(password);
    
    // Run backup in background thread
    Task<Void> backupTask = new Task<Void>() {
        @Override
        protected Void call() throws Exception {
            updateMessage("Starting backup...");
            
            if ("postgresql".equals(dbType)) {
                PostgreSQL.backup(host, user, encryptedPass, dbName, label);
            } else if ("mysql".equals(dbType)) {
                MysqlSQL.backup(host, user, encryptedPass, dbName, label);
            }
            
            updateMessage("Backup completed successfully!");
            return null;
        }
    };
    
    // Update UI on progress
    backupTask.messageProperty().addListener((obs, oldMsg, newMsg) -> {
        outputArea.appendText(newMsg + "\n");
    });
    
    // Start backup
    new Thread(backupTask).start();
}
```

### **Step 2: Update Main.fxml**

Add missing fields:
- Host field
- Label field  
- Database Type ComboBox

### **Step 3: Update pom.xml**

Ensure JavaFX dependencies are properly configured (already done based on your pom.xml).

---

## 🏃 How to Start

### **Option A: Start GUI Application**

```bash
# Using Maven JavaFX plugin
mvn clean javafx:run

# OR compile and run manually
mvn clean compile
java --module-path /path/to/javafx/lib \
     --add-modules javafx.controls,javafx.fxml \
     -cp target/classes \
     com.fabriciojf.postgresql.backup.gui.MainApp
```

### **Option B: Start CLI Application**

```bash
# Run all registered backups
java -jar target/database_backup.jar

# Encrypt a password
java -jar target/database_backup.jar mypassword
```

---

## 📋 Integration Checklist

### Immediate Tasks:

1. **✅ Understand Backend Flow** (You're here!)
2. **⏳ Update MainController.java** - Connect to backend
3. **⏳ Update Main.fxml** - Add missing fields
4. **⏳ Test Integration** - Run a backup through GUI
5. **⏳ Add Error Handling** - Show user-friendly messages
6. **⏳ Add Progress Feedback** - Real-time output

---

## 🔍 Key Backend Methods You'll Use

```java
// Encrypt password before storing/using
String encrypted = HashHelper.encrypt("mypassword");

// Execute PostgreSQL backup
PostgreSQL.backup(host, user, encryptedPass, database, label);

// Execute MySQL backup
MysqlSQL.backup(host, user, encryptedPass, database, label);

// Get backup directory
String backupDir = SettingsHelper.pathBackups;
```

---

## ⚠️ Important Notes

1. **Password Encryption**: Always encrypt passwords before passing to backup methods
2. **Async Execution**: Backup operations are blocking - use JavaFX Task/Service
3. **Path Configuration**: Backend uses `/etc/database_backup/` on Linux, may need Windows adjustment
4. **Error Handling**: Backup commands can fail - catch and display errors
5. **Output Streaming**: Capture process output for real-time feedback

---

**Ready to code?** Start with updating `MainController.java`!


