<<<<<<< HEAD
# AIAGENT-Smart-HRD-Agent
 Proof-of-Concept (PoC) for a Smart HR Agent implemented in Java. The agent acts as a backend CLI application designed to understand employee natural language requests in Bahasa Indonesia
=======
# Smart HR Agent - PoC of Ai Agents based on Java

## Overview

This project is a Proof-of-Concept (PoC) for a Smart HR Agent implemented in Java. The agent acts as a backend CLI application designed to understand employee natural language requests in Bahasa Indonesia, process these requests by searching data or executing HR actions, and return relevant responses or confirmations.

The key idea is to build a modular, extensible system that:
- Can differentiate between questions (queries for data) and commands (actions to execute),
- Retrieves HR data from CSV files (employees, leave balances, leave requests, performance reviews),
- Uses a lightweight Large Language Model (LLM) locally (Qwen2.5:3b via Ollama) for robust natural language understanding and extraction of structured commands in JSON format,
- Executes mock HR functions based on extracted intents,
- Operates interactively via a simple CLI interface emphasizing backend logic over UI.

## Features

- **Intent Detection:** Rule-based detection to classify input as a question or an action command.
- **Query Service:** Efficient lookup of employee information and leave balances from CSV.
- **Action Service:** Uses LLM to parse flexible natural language commands with typos into structured intents for execution.
- **LLM Integration:** Uses Ollama running Qwen2.5:3b locally for fast and reliable text-to-JSON extraction.
- **CSV-based Data Layer:** Employees, leave balances, leave requests, and performance reviews loaded into in-memory data structures.
- **Command-line Interface:** Interactive user interface for demoing querying and command execution with natural Indonesian language input.
- **Mock HR Functions:** Simulated responses to validate actions like leave application, review scheduling, leave status checking, etc.

## Technologies & Tools

- Java 17 (OpenJDK)
- Maven as build and dependency manager
- OpenCSV for CSV parsing
- Jackson for JSON binding
- Ollama local LLM runtime
- Qwen2.5:3b model for natural language understanding
- CLI built with Java Scanner for interactive user input

## Design Highlights

- Modular layers separate data, query, action, and natural language understanding.
- Simplified CSV data for rapid prototyping with ability to swap into database later.
- LLM usage limits natural language parsing complexity, enabling flexible, fuzzy commands in Indonesian.
- CLI focuses on core business logic without dependency on UI complexity.


## How to Run

1. Clone or download the project.
2. Install Java 17 and Maven.
3. Install Ollama local runtime and pull Qwen2.5:3b model
4. Build the project: mvn clean package
5. Run the interactive CLI: mvn exec:java -Dexec.mainClass="com.hragent.MainApp"
6. Use natural Bahasa Indonesia commands or questions, e.g.:
- "siapa manajer budi?"
- "tolong apply cuti tahunan buat rina dari tgl 3 okt sampai 5 okt"
7. Type "quit" for close program



## Acknowledgements

This project leverages open source technologies including [Ollama](https://ollama.com) and [LangChain4j](https://github.com/langchain4j/langchain4j), and applies AI-assisted development to build a robust backend system demonstrating state-of-the-art natural language understanding.

>>>>>>> 0074876 (base agent hr with java)
