# java-client-server-system
# Java Client-Server Role-Based System

## Description

This project is a client-server application developed in Java using sockets and multithreading. It simulates a role-based system with different user types (Admin, Student, Teacher), each having specific functionalities.

##  Features

* Client-server architecture using Java sockets
* Multi-client handling using threads (ClientHandler)
* Role-based system (Admin / Student / Teacher)
* User authentication (login system)
* Grade management system
* Input validation using regular expressions
* Data persistence using file serialization

##  Technologies Used

* Java
* Sockets (TCP)
* Multithreading
* OOP principles (Inheritance, Polymorphism, Enums)
* File I/O (Serialization)

##  How it works

* Server accepts multiple client connections
* Each client is handled in a separate thread
* Users log in with credentials
* Each role has different permissions and actions

##  Roles

* **Admin** → creates users
* **Teacher** → assigns grades
* **Student** → views grades

##  Project Structure

* Server
* ClientHandler
* Client
* SystemManager
* User classes (Admin, Student, Teacher)
* Grade system
