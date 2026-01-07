# E-Governance Grievance Redressal System

The proposed **E-Governance Grievance Redressal System** is a centralized platform built using **Angular 21** and **Spring Boot 3.2.5** that enables citizens to submit and track grievances online. The system automatically routes complaints to the appropriate departments, monitors the grievance lifecycle, and provides real-time status updates and dashboards to ensure transparency, accountability, and timely resolution.

<p align="left">
  <img src="https://img.shields.io/badge/Angular-21-DD0031?style=for-the-badge&logo=angular&logoColor=white" />
  <img src="https://img.shields.io/badge/Spring%20Boot-3.2.5-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" />
  <img src="https://img.shields.io/badge/WebFlux-Reactive-0A7EA4?style=for-the-badge" />
  <img src="https://img.shields.io/badge/Jenkins-CI%2FCD-D24939?style=for-the-badge&logo=jenkins&logoColor=white" />
  <img src="https://img.shields.io/badge/Docker-Containerized-2496ED?style=for-the-badge&logo=docker&logoColor=white" />
  <img src="https://img.shields.io/badge/Netlify-Frontend-00C7B7?style=for-the-badge&logo=netlify&logoColor=white" />
  <img src="https://img.shields.io/badge/Azure-VM-0078D4?style=for-the-badge&logo=microsoftazure&logoColor=white" />
</p>

---
## Live Links

### Live on netlify

https://egrievance-and-redressal.netlify.app/

### Backend on Azure VM - Eureka Server

[http://20.244.2.109:8761](http://20.244.2.109:8761)

---

## Features

<ul>
  <li>Centralized grievance submission and tracking system</li>
  <li>Microservices architecture with Spring WebFlux</li>
  <li>CI/CD pipeline implemented using Jenkins</li>
  <li>Containerized services using Docker</li>
  <li>Service discovery using Eureka Server</li>
  <li>Frontend deployed on Netlify and backend hosted on Azure VM</li>
</ul>

-----

## Links

<table border="1" cellpadding="8" cellspacing="0">
  <tr>
    <th>Component</th>
    <th>Repository / Document</th>
  </tr>
  <tr>
    <td>Frontend</td>
    <td>
      <a href="https://github.com/kanchanraiii/EGovGrievance_Frontend" target="_blank">
        Frontend Repository
      </a>
    </td>
  </tr>
  <tr>
    <td>Design Document</td>
    <td>
      <a href="https://github.com/kanchanraiii/EGovGrievance_Backend/blob/main/Results%20Document.docx">
        Design Document.docx
      </a>
    </td>
  </tr>
  <tr>
    <td>Results Document</td>
    <td>
      <a href="https://github.com/kanchanraiii/EGovGrievance_Backend/blob/main/Results%20Document.docx">
        Results Document.docx
      </a>
    </td>
  </tr>
</table>

---

## Web Preview

<table>
  <tr>
    <td align="center">
      <strong>Home Page</strong><br />
      <img src="https://github.com/kanchanraiii/EGovGrievance_Backend/blob/main/images/homepage.png" width="450"/>
    </td>
    <td align="center">
      <strong>Grievance Dashboard</strong><br />
      <img src="https://github.com/kanchanraiii/EGovGrievance_Backend/blob/main/images/Screenshot%202026-01-07%20000839.png" width="450"/>
    </td>
  </tr>
</table>

---

## Backend Preview (Azure VM)

<table>
  <tr>
    <td align="center">
      <strong>Docker containers running on VM</strong><br />
      <img src="https://github.com/kanchanraiii/EGovGrievance_Backend/blob/main/images/Screenshot%202026-01-07%20001057.png" width="450"/>
    </td>
    <td align="center">
      <strong>Azure Deployment VM</strong><br />
      <img src="https://github.com/kanchanraiii/EGovGrievance_Backend/blob/main/images/Screenshot%202026-01-07%20001126.png" width="450"/>
    </td>
  </tr>
</table>

---

## Jenkins CI/CD Preview

<table>
  <tr>
    <td align="center">
      <strong>Jenkins Build Preview</strong><br />
      <img src="https://github.com/kanchanraiii/EGovGrievance_Backend/blob/main/images/Screenshot%202026-01-07%20000958.png" width="450"/>
    </td>
    <td align="center">
      <strong>Jenkins GitHub Webhook</strong><br />
      <img src="https://github.com/kanchanraiii/EGovGrievance_Backend/blob/main/images/Screenshot%202026-01-07%20001241.png" width="450"/>
    </td>
  </tr>
</table>

------

## SonarQube Code Quality Report

<table>
  <tr>
    <td align="center">
      <strong>94.1% Coverage</strong><br />
      <img src="https://github.com/kanchanraiii/EGovGrievance_Backend/blob/main/images/Screenshot%202026-01-07%20002453.png" width="700"/>
    </td>
    <td align="center">
      <strong>SonarQube Analysis Dashboard</strong><br />
      <img src="https://github.com/kanchanraiii/EGovGrievance_Backend/blob/main/images/Screenshot%202026-01-07%20002631.png" width="700"/>
    </td>
  </tr>
</table>

---



---

## User Roles

<table border="1" cellpadding="8" cellspacing="0" 
       style="border-collapse: collapse; text-align: center; vertical-align: middle; width: 100%;">
  <tr>
    <th>User</th>
    <th>Role</th>
  </tr>
  <tr>
    <td>Citizen</td>
    <td>Submits and tracks grievances</td>
  </tr>
  <tr>
    <td>Admin</td>
    <td>Manages staff users and departments</td>
  </tr>
  <tr>
    <td>Department Officer</td>
    <td>Manages grievances and assigns them to case workers</td>
  </tr>
  <tr>
    <td>Supervisory Officer</td>
    <td>Resolves escalated grievances</td>
  </tr>
  <tr>
    <td>Case Worker</td>
    <td>Handles assigned grievances and updates status</td>
  </tr>  
</table>

---

## API Endpoints

### Auth-Service
<table>
  <thead><tr><th>Method</th><th>Path</th><th>Allowed Roles</th></tr></thead>
  <tbody>
    <tr><td>POST</td><td>/api/auth/register</td><td>Permit all</td></tr>
    <tr><td>POST</td><td>/api/auth/citizen/register</td><td>Permit all</td></tr>
    <tr><td>POST</td><td>/api/auth/case-worker/register</td><td>DEPARTMENT_OFFICER, ADMIN</td></tr>
    <tr><td>POST</td><td>/api/auth/admin/register</td><td>Permit all</td></tr>
    <tr><td>POST</td><td>/api/auth/login</td><td>Permit all</td></tr>
    <tr><td>GET</td><td>/api/auth/profile</td><td>Any authenticated user</td></tr>
  </tbody>
</table>

### Storage-Service
<table>
  <thead><tr><th>Method</th><th>Path</th><th>Allowed Roles</th></tr></thead>
  <tbody>
    <tr><td>POST</td><td>/api/files/upload</td><td>All authenticated roles</td></tr>
    <tr><td>GET</td><td>/api/files/{fileId}</td><td>Any authenticated user</td></tr>
  </tbody>
</table>

### Feedback-Service
<table>
  <thead><tr><th>Method</th><th>Path</th><th>Allowed Roles</th></tr></thead>
  <tbody>
    <tr><td>POST</td><td>/api/feedback/add-feedback</td><td>CITIZEN</td></tr>
    <tr><td>GET</td><td>/api/feedback/grievance/{grievanceId}</td><td>Authorized roles</td></tr>
  </tbody>
</table>

### Notification-Service
<table>
  <thead><tr><th>Method</th><th>Path</th><th>Allowed Roles</th></tr></thead>
  <tbody>
    <tr><td>GET</td><td>/api/notifications/user/{userId}</td><td>Any authenticated user</td></tr>
  </tbody>
</table>

### Grievance-Service
<table>
  <thead><tr><th>Method</th><th>Path</th><th>Allowed Roles</th></tr></thead>
  <tbody>
    <tr><td>POST</td><td>/api/grievances/create</td><td>CITIZEN</td></tr>
    <tr><td>GET</td><td>/api/grievances/{id}</td><td>Any authenticated user</td></tr>
    <tr><td>PATCH</td><td>/api/grievances/status</td><td>Authorized roles</td></tr>
  </tbody>
</table>

---

<hr />
<p align="center">
  Made by 
  <a href="https://kanchanraiii.xyz" target="_blank">
    Kanchan Rai 
  </a>
  
</p>
