# Dropbox like Web Application (Fullstack Project)

## Quick Start
#### for local run using h2 database :
```bash
git clone <your_repo>
```
get the server running using the following commands : 
```bash
cd server
$env:SPRING_PROFILES_ACTIVE="dev"
mvn spring-boot:run
```
get the frontend running using the following commands : 
```bash
cd frontend
npm install
npm run build
npm run dev
```
#### {WIP} for prod run using postgresql and docker : 
```bash
git clone <your_repo>
cd dropbox-clone
docker-compose up --build
```

## Features
- Upload files
- View file list
- Download files
