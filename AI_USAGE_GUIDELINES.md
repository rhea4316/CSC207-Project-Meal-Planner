# AI Usage Guidelines for Meal Planner Project

**Team 14 (TUT0201)**  
**Based on Course Syllabus**

---

## 📜 Course Policy

> "The use of Generative AI is allowed throughout the course. With this said, we caution you to not rely on these tools to complete your work. Instead, we recommend using such tools to engage with the course material as you learn. Ultimately, you are responsible for your own learning in this course, and for all the work you submit for credit. It is your responsibility to critically evaluate the content generated, and to regularly assess your own learning independent of generative AI tools."

**Key Points:**
- ✅ AI use is **allowed**
- ⚠️ Don't **rely** on AI to complete work
- 📚 Use AI as a **learning tool**
- 🎯 You are **responsible** for your work and learning
- 🔍 **Critically evaluate** AI-generated content
- ✅ Be able to work **independently** of AI

---

## 🎯 Practical Guidelines for Our Project

### ✅ GOOD Uses of AI (Learning Tool)

1. **Understanding Concepts**
   - "Explain Clean Architecture in simple terms"
   - "How do I implement equals/hashCode for immutable classes?"
   - "What's the difference between an Interactor and a Controller?"

2. **Code Examples for Learning**
   - "Show me an example of a value object pattern"
   - "How do I structure a repository interface?"
   - Use examples to **understand**, then write your own

3. **Debugging Help**
   - "Why is this error happening?" (understand the error)
   - "How do I fix this compilation error?" (learn from explanation)

4. **Code Review**
   - "Review this code for best practices"
   - "Suggest improvements to this method"
   - **You decide** what to accept/reject

### ❌ AVOID These (Don't Rely on AI)

1. **Copy-Paste Without Understanding**
   - ❌ Using AI-generated code without reading it
   - ❌ Not knowing what the code does
   - ❌ Can't explain how it works

2. **Complete Features by AI**
   - ❌ "Write my entire Interactor for me"
   - ❌ "Implement my whole use case"
   - ✅ Instead: Design it yourself, use AI for **specific parts** you're stuck on

3. **Bypassing Learning**
   - ❌ Using AI to skip learning Clean Architecture
   - ❌ Not understanding dependencies between layers
   - ❌ Can't explain your design decisions

---

## 📋 Code-Specific Recommendations

### 🟡 Entity Classes

**Recommendation: Try to write yourself, use AI as reference**

**Why?**
- Entities are the **foundation** of your project
- They're relatively **simple** (good learning opportunity)
- You need to **understand** the data model
- You'll be asked about them in presentations

**If using AI:**
1. ✅ Understand what each field does
2. ✅ Know why you chose immutable vs mutable
3. ✅ Be able to explain equals/hashCode logic
4. ✅ Understand business methods (if any)

**Example:**
```
✅ Good: "I looked at AI examples of value objects, understood the pattern, 
         then wrote my NutritionInfo class following that pattern"

❌ Bad: "AI wrote my Recipe class, I don't know how it works"
```

### 🔴 Use Case Interactors

**Recommendation: Write yourself, use AI minimally**

**Why?**
- This is your **core business logic** (the point of the assignment!)
- Each person has their own use case (your responsibility)
- Presenters will ask about your logic
- This is where you demonstrate **understanding**

**AI can help with:**
- ✅ Specific Java syntax questions
- ✅ Error handling patterns
- ✅ Understanding Clean Architecture boundaries

**AI should NOT:**
- ❌ Write your entire interactor
- ❌ Design your use case flow

### 🟢 Controllers, Presenters, ViewModels

**Recommendation: Write yourself (or with minimal AI help)**

**Why?**
- You need to understand how layers communicate
- These connect your use case to the UI
- Important for Clean Architecture understanding

**AI can help with:**
- ✅ Specific Swing/AWT questions (if doing GUI)
- ✅ Design pattern examples (Observer, etc.)

### 🔵 Views (GUI)

**Recommendation: More flexibility, but still understand**

**Why?**
- GUI code can be verbose and tedious
- More "mechanical" than business logic
- But you should still understand the structure

**AI can help with:**
- ✅ Swing component setup
- ✅ Event handling patterns
- ✅ Layout managers

**But:**
- ✅ You should understand what each component does
- ✅ You should know how data flows (View → Controller → Interactor)

### 🆘 Exception Classes, Utility Classes

**Recommendation: Moderate AI use OK**

**Why?**
- These are more "supporting" code
- Less critical for demonstrating understanding
- But still part of the project

**AI can help with:**
- ✅ Standard exception patterns
- ✅ Utility method implementations
- ✅ Helper functions

**Still:**
- ✅ Understand what the code does
- ✅ Know when/how to use it

---

## ✅ Self-Check Questions

Before submitting any code (especially AI-assisted), ask yourself:

1. **Can I explain this code line-by-line?**
   - If not → Don't submit it, study it first

2. **Do I understand WHY it's designed this way?**
   - Architecture decisions
   - Design patterns used
   - Trade-offs made

3. **Can I modify/extend this code?**
   - If you can't change it, you don't understand it

4. **Can I explain it in a presentation?**
   - TA/Professor will ask questions
   - You need to answer confidently

5. **Could I write something similar from scratch?**
   - Maybe not perfect, but the core structure

---

## 🎓 Learning-First Approach

### Example Workflow:

**BAD Workflow:**
```
1. Ask AI: "Write my Recipe entity"
2. Copy-paste code
3. Submit
❌ You learned nothing
```

**GOOD Workflow:**
```
1. Design Recipe class on paper (what fields? methods?)
2. Read README/CODE_STRUCTURE.md for requirements
3. If stuck: Ask AI "Show me example of immutable value object"
4. Understand the pattern
5. Write Recipe class yourself following the pattern
6. Test it
7. Review and improve
✅ You learned the concept and can explain it
```

---

## 📝 Documenting AI Use (Optional but Recommended)

If you use AI significantly, consider adding a comment:

```java
/**
 * This class follows the value object pattern.
 * I used AI to understand the pattern, then implemented it myself.
 * Key learning: Immutability ensures thread safety and prevents bugs.
 */
public class NutritionGoals {
    // Your code here
}
```

**Why?**
- Shows **honesty** about your process
- Demonstrates you **learned** from AI (not just copied)
- Helps you remember what you learned

---

## 🚨 Red Flags (What Not to Do)

1. ❌ **Submitting code you can't explain**
2. ❌ **Using AI to skip learning Clean Architecture**
3. ❌ **Copying entire features without understanding**
4. ❌ **Not testing AI-generated code**
5. ❌ **Not reviewing/critiquing AI suggestions**

---

## 💡 Bottom Line

**Remember:** The goal is to **learn** Clean Architecture, Java, and software engineering. AI is a **tool to help you learn**, not a tool to skip learning.

**Ask yourself:** "If I had to explain this code to my TA/professor, could I do it confidently?"

If yes → You're good!  
If no → Spend more time understanding before submitting.

---

## 📚 Resources for Learning (Before Using AI)

1. **README.md** - Project overview and requirements
2. **CODE_STRUCTURE.md** - Clean Architecture explanation
3. **HOW_TO_WORK.md** - Step-by-step guide
4. **Java Documentation** - Official Java docs
5. **Clean Architecture Book** (if available)
6. **Course Materials** - Lectures, slides, notes

**Try these first, then use AI for specific questions!**

---

**Last Updated**: November 4, 2025  
**Questions?** Discuss with team or ask on Piazza!
