# 🏥 MedTrack Backend

MedTrack is a real-world backend system built with **Spring Boot**.  
The goal of this project is to simulate a **professional software development environment**.

Students will work using:
- Jira-like tickets
- Git branching strategy
- Pull Request (PR) workflow
- Code reviews

---

# 🚀 Tech Stack

- Java 21
- Spring Boot 3
- Spring Web
- Spring Data JPA
- H2 Database (development)
- Maven

---

# ▶️ How to Run the Project

```bash
./mvnw spring-boot:run
```

App runs on:

```
http://localhost:8080
```

H2 Console:

```
http://localhost:8080/h2-console
```

---

# 🧠 Project Structure

```
com.medtrack
 ├── controller   → API layer
 ├── service      → business logic
 ├── repository   → database access
 ├── entity       → database models
 └── dto          → request/response objects
```

---

# 🧩 How We Work (IMPORTANT)

This is NOT a personal project.

We follow **real team workflow**:

```
Ticket → Branch → Code → Push → Pull Request → Review → Merge
```

---

# 🟩 1. Before You Start a Ticket

✔ Make sure:

* The ticket is assigned to YOU
* You understand the task
* You checked dependencies

❌ DO NOT:

* Work on unassigned tickets
* Change other people’s code

---

# 🔄 2. Always Sync Before Starting (VERY IMPORTANT)

Before creating your branch, always run:

```bash
git checkout main
git pull origin main
```

Then create your branch:

```bash
git checkout -b feature/TICKET-ID-short-name
```

Example:

```bash
git checkout -b feature/T6-user-register
```

❌ If you skip this step:

* You WILL get conflicts
* You MAY break the project

---

# 🟩 3. Coding Rules

## 🔴 STRICT RULES

❌ DO NOT:

* Change database schema (Entities) without permission
* Modify other services/controllers
* Push broken code

---

## 🟢 FOLLOW THESE RULES

* Use **Controller → Service → Repository**
* Use **constructor injection ONLY**
* Keep code **simple and readable**
* No unused code
* No debug print statements

---

# 🟩 4. Before You Push

Before pushing your code:

✔ Project runs without errors
✔ Your API works
✔ No red errors in IntelliJ
✔ Only your files are changed
✔ Code is clean

---

# 🧪 4.1 API Testing (MANDATORY)

Before pushing your code:

✔ Test your API using:

* Postman
* curl

✔ Verify:

* Correct response
* No errors
* Data saved in DB

---

# 🟩 5. Commit Rules

Use clear commit messages:

```bash
git commit -m "T6: implement user registration API"
```

---

# 🟩 6. Push Code

```bash
git push origin feature/TICKET-ID-short-name
```

---

# 🔀 6.1 Pull Request Base Rule

All Pull Requests must target:

```
main branch
```

❌ DO NOT:

* Open PR to other branches
* Merge directly into main

---

# 🟩 7. Create Pull Request (PR)

After pushing:

1. Go to GitHub
2. Click **Compare & Pull Request**

### PR Title:

```
T6 - User Registration API
```

### PR Description:

* What you implemented
* How to test it

---

# 🟩 8. Pull Request Rules

❌ DO NOT:

* Merge your own PR
* Skip review

---

## 🟢 PR MUST:

* Match ticket requirements
* Be fully working
* Be clean and readable

---

# 👀 8.1 Code Review Responsibility

Each PR must be reviewed by:

* Instructor (Admin) OR
* Assigned reviewer

❌ DO NOT:

* Approve your own PR
* Ignore review comments

---

# 🟩 9. Code Review Process

```
PR → Review → Changes → Fix → Approved → Merge
```

---

# ⚠️ 10. Merge Conflict Rule

If you get a conflict:

```bash
git pull origin main
```

Fix conflicts locally, then:

```bash
git add .
git commit
git push
```

❌ If you don’t understand conflicts:
→ ASK before pushing

---

# 🧱 10.1 File Ownership Rule

Each student owns ONLY their ticket scope.

❌ DO NOT:

* Modify another student's Entity
* Modify shared classes without permission

✔ If you need change:
→ Discuss first

---

# 🟩 11. Common Mistakes (Avoid These)

❌ Pushing code that doesn't run
❌ Changing other people's code
❌ Not following the ticket
❌ Huge messy commits
❌ No testing

---

# 🟩 12. Branch Naming Rules

```
feature/TICKET-ID-name
fix/TICKET-ID-name
```

---

# 🟩 13. Definition of Done

Your ticket is DONE only if:

✔ Code works
✔ API tested
✔ PR approved
✔ Merged to main

---

# 🟩 14. Final Rule

```
Act like a professional developer.
```

---

# 🆘 Need Help?

Ask BEFORE breaking the system 🙂
