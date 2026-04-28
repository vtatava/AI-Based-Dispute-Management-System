# Cloud Deployment Guide - AI-Based Dispute Management System

This guide provides step-by-step instructions to deploy your application on **IBM Cloud** and **AWS**.

---

## Table of Contents
1. [Prerequisites](#prerequisites)
2. [IBM Cloud Deployment](#ibm-cloud-deployment)
3. [AWS Deployment](#aws-deployment)
4. [Post-Deployment Configuration](#post-deployment-configuration)
5. [Cost Optimization Tips](#cost-optimization-tips)

---

## Prerequisites

### General Requirements
- Git installed
- Application source code
- Cloud provider account (IBM Cloud or AWS)
- Domain name (optional, for custom URLs)

### Application Components
- **Backend**: Spring Boot (Java) application on port 9090
- **Frontend**: React application on port 3000
- **Database**: H2 (file-based) - needs migration to cloud database
- **External Services**: IBM ICA, Ollama/GPT-4o-mini, Tesseract OCR

---

## IBM Cloud Deployment

### Option 1: IBM Cloud Foundry (Recommended for Quick Start)

#### Step 1: Install IBM Cloud CLI
```bash
# Windows (PowerShell)
iex(New-Object Net.WebClient).DownloadString('https://clis.cloud.ibm.com/install/powershell')

# Verify installation
ibmcloud --version
```

#### Step 2: Login to IBM Cloud
```bash
ibmcloud login
# Or with SSO
ibmcloud login --sso

# Target your organization and space
ibmcloud target --cf
```

#### Step 3: Prepare Backend for Deployment

Create `manifest.yml` in the backend directory:
```yaml
applications:
- name: dispute-ai-backend
  memory: 1G
  instances: 1
  buildpack: java_buildpack
  path: target/dispute-ai-0.0.1-SNAPSHOT.jar
  env:
    JBP_CONFIG_OPEN_JDK_JRE: '{ jre: { version: 17.+ } }'
    SPRING_PROFILES_ACTIVE: cloud
  services:
    - dispute-db
```

Create `application-cloud.properties` in `backend/src/main/resources/`:
```properties
# Cloud-specific configuration
server.port=${PORT:9090}

# Use IBM Db2 or PostgreSQL instead of H2
spring.datasource.url=${VCAP_SERVICES_DISPUTE_DB_CREDENTIALS_URL}
spring.datasource.username=${VCAP_SERVICES_DISPUTE_DB_CREDENTIALS_USERNAME}
spring.datasource.password=${VCAP_SERVICES_DISPUTE_DB_CREDENTIALS_PASSWORD}

# IBM ICA Configuration (already configured)
ibm.ica.enabled=true
ibm.ica.api-key=${IBM_ICA_API_KEY}

# Disable file-based features for cloud
ocr.enabled=false
tesseract.datapath=

# Production settings
spring.jpa.hibernate.ddl-auto=update
logging.level.com.app=INFO
```

#### Step 4: Build and Deploy Backend
```bash
# Build the application
cd backend
mvn clean package -DskipTests

# Create database service
ibmcloud cf create-service databases-for-postgresql standard dispute-db

# Deploy to IBM Cloud
ibmcloud cf push

# Set environment variables
ibmcloud cf set-env dispute-ai-backend IBM_ICA_API_KEY "your-ica-api-key"
ibmcloud cf restage dispute-ai-backend
```

#### Step 5: Prepare Frontend for Deployment

Create `.env.production` in frontend directory:
```env
REACT_APP_API_URL=https://dispute-ai-backend.mybluemix.net
```

Build the frontend:
```bash
cd frontend
npm run build
```

Create `manifest.yml` in frontend directory:
```yaml
applications:
- name: dispute-ai-frontend
  memory: 256M
  instances: 1
  buildpack: staticfile_buildpack
  path: build
  env:
    FORCE_HTTPS: true
```

Create `Staticfile` in frontend directory:
```
root: build
location_include: includes/*.conf
```

Create `includes/nginx.conf`:
```nginx
location / {
  try_files $uri $uri/ /index.html;
}
```

#### Step 6: Deploy Frontend
```bash
cd frontend
ibmcloud cf push
```

### Option 2: IBM Cloud Code Engine (Containerized)

#### Step 1: Install Docker
Download and install Docker Desktop from https://www.docker.com/

#### Step 2: Create Dockerfiles

**Backend Dockerfile** (`backend/Dockerfile`):
```dockerfile
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 9090
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Frontend Dockerfile** (`frontend/Dockerfile`):
```dockerfile
FROM node:18-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/build /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

**Frontend nginx.conf** (`frontend/nginx.conf`):
```nginx
server {
    listen 80;
    location / {
        root /usr/share/nginx/html;
        index index.html;
        try_files $uri $uri/ /index.html;
    }
}
```

#### Step 3: Build and Push Docker Images
```bash
# Login to IBM Cloud Container Registry
ibmcloud cr login

# Create namespace
ibmcloud cr namespace-add dispute-ai

# Build and push backend
cd backend
docker build -t us.icr.io/dispute-ai/backend:latest .
docker push us.icr.io/dispute-ai/backend:latest

# Build and push frontend
cd ../frontend
docker build -t us.icr.io/dispute-ai/frontend:latest .
docker push us.icr.io/dispute-ai/frontend:latest
```

#### Step 4: Deploy to Code Engine
```bash
# Create Code Engine project
ibmcloud ce project create --name dispute-ai-project
ibmcloud ce project select --name dispute-ai-project

# Deploy backend
ibmcloud ce application create \
  --name dispute-ai-backend \
  --image us.icr.io/dispute-ai/backend:latest \
  --port 9090 \
  --min-scale 1 \
  --max-scale 3 \
  --cpu 1 \
  --memory 2G \
  --env IBM_ICA_API_KEY=your-api-key

# Deploy frontend
ibmcloud ce application create \
  --name dispute-ai-frontend \
  --image us.icr.io/dispute-ai/frontend:latest \
  --port 80 \
  --min-scale 1 \
  --max-scale 2 \
  --cpu 0.5 \
  --memory 1G \
  --env REACT_APP_API_URL=https://dispute-ai-backend.xxx.appdomain.cloud
```

---

## AWS Deployment

### Option 1: AWS Elastic Beanstalk (Recommended for Quick Start)

#### Step 1: Install AWS CLI and EB CLI
```bash
# Install AWS CLI
# Download from: https://aws.amazon.com/cli/

# Install EB CLI
pip install awsebcli

# Configure AWS credentials
aws configure
```

#### Step 2: Prepare Backend for Elastic Beanstalk

Create `application-aws.properties` in `backend/src/main/resources/`:
```properties
# AWS-specific configuration
server.port=5000

# Use AWS RDS PostgreSQL
spring.datasource.url=jdbc:postgresql://${RDS_HOSTNAME}:${RDS_PORT}/${RDS_DB_NAME}
spring.datasource.username=${RDS_USERNAME}
spring.datasource.password=${RDS_PASSWORD}

# IBM ICA Configuration
ibm.ica.enabled=true
ibm.ica.api-key=${IBM_ICA_API_KEY}

# Production settings
spring.jpa.hibernate.ddl-auto=update
logging.level.com.app=INFO
```

Create `.ebextensions/01-environment.config` in backend:
```yaml
option_settings:
  aws:elasticbeanstalk:application:environment:
    SPRING_PROFILES_ACTIVE: aws
    IBM_ICA_API_KEY: your-ica-api-key
  aws:elasticbeanstalk:container:java:
    JVMOptions: '-Xmx512m'
```

#### Step 3: Deploy Backend to Elastic Beanstalk
```bash
cd backend

# Build the application
mvn clean package -DskipTests

# Initialize EB
eb init -p java-17 dispute-ai-backend --region us-east-1

# Create environment with RDS
eb create dispute-ai-backend-env \
  --database.engine postgres \
  --database.username dbadmin \
  --database.password YourSecurePassword123

# Deploy
eb deploy

# Set environment variables
eb setenv IBM_ICA_API_KEY=your-api-key

# Open in browser
eb open
```

#### Step 4: Deploy Frontend to S3 + CloudFront

Create `.env.production` in frontend:
```env
REACT_APP_API_URL=http://dispute-ai-backend-env.xxx.elasticbeanstalk.com
```

Build and deploy:
```bash
cd frontend

# Build
npm run build

# Create S3 bucket
aws s3 mb s3://dispute-ai-frontend

# Enable static website hosting
aws s3 website s3://dispute-ai-frontend \
  --index-document index.html \
  --error-document index.html

# Upload build files
aws s3 sync build/ s3://dispute-ai-frontend --acl public-read

# Create CloudFront distribution (optional, for HTTPS and CDN)
aws cloudfront create-distribution \
  --origin-domain-name dispute-ai-frontend.s3.amazonaws.com
```

### Option 2: AWS ECS (Containerized with Fargate)

#### Step 1: Create ECR Repositories
```bash
# Create repositories
aws ecr create-repository --repository-name dispute-ai-backend
aws ecr create-repository --repository-name dispute-ai-frontend

# Login to ECR
aws ecr get-login-password --region us-east-1 | \
  docker login --username AWS --password-stdin \
  123456789012.dkr.ecr.us-east-1.amazonaws.com
```

#### Step 2: Build and Push Docker Images
```bash
# Build and push backend
cd backend
docker build -t dispute-ai-backend .
docker tag dispute-ai-backend:latest \
  123456789012.dkr.ecr.us-east-1.amazonaws.com/dispute-ai-backend:latest
docker push 123456789012.dkr.ecr.us-east-1.amazonaws.com/dispute-ai-backend:latest

# Build and push frontend
cd ../frontend
docker build -t dispute-ai-frontend .
docker tag dispute-ai-frontend:latest \
  123456789012.dkr.ecr.us-east-1.amazonaws.com/dispute-ai-frontend:latest
docker push 123456789012.dkr.ecr.us-east-1.amazonaws.com/dispute-ai-frontend:latest
```

#### Step 3: Create ECS Cluster and Services

Create `task-definition-backend.json`:
```json
{
  "family": "dispute-ai-backend",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "1024",
  "memory": "2048",
  "containerDefinitions": [
    {
      "name": "backend",
      "image": "123456789012.dkr.ecr.us-east-1.amazonaws.com/dispute-ai-backend:latest",
      "portMappings": [
        {
          "containerPort": 9090,
          "protocol": "tcp"
        }
      ],
      "environment": [
        {
          "name": "SPRING_PROFILES_ACTIVE",
          "value": "aws"
        },
        {
          "name": "IBM_ICA_API_KEY",
          "value": "your-api-key"
        }
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/dispute-ai-backend",
          "awslogs-region": "us-east-1",
          "awslogs-stream-prefix": "ecs"
        }
      }
    }
  ]
}
```

Deploy to ECS:
```bash
# Create cluster
aws ecs create-cluster --cluster-name dispute-ai-cluster

# Register task definition
aws ecs register-task-definition --cli-input-json file://task-definition-backend.json

# Create service with load balancer
aws ecs create-service \
  --cluster dispute-ai-cluster \
  --service-name dispute-ai-backend-service \
  --task-definition dispute-ai-backend \
  --desired-count 2 \
  --launch-type FARGATE \
  --network-configuration "awsvpcConfiguration={subnets=[subnet-xxx],securityGroups=[sg-xxx],assignPublicIp=ENABLED}"
```

---

## Post-Deployment Configuration

### 1. Database Migration
After deployment, migrate from H2 to cloud database:

**For PostgreSQL:**
```sql
-- Export data from H2
-- Import to PostgreSQL using pg_dump/pg_restore or DBeaver
```

### 2. Environment Variables
Set these in your cloud platform:
- `IBM_ICA_API_KEY`: Your IBM ICA API key
- `SPRING_PROFILES_ACTIVE`: cloud or aws
- `DATABASE_URL`: Cloud database connection string

### 3. CORS Configuration
Update backend CORS settings for production:
```java
@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                    .allowedOrigins("https://your-frontend-domain.com")
                    .allowedMethods("GET", "POST", "PUT", "DELETE");
            }
        };
    }
}
```

### 4. SSL/HTTPS Setup
- **IBM Cloud**: Automatic with Cloud Foundry/Code Engine
- **AWS**: Use AWS Certificate Manager + CloudFront/ALB

### 5. Monitoring and Logging
- **IBM Cloud**: Use IBM Log Analysis and Monitoring
- **AWS**: Use CloudWatch Logs and Metrics

---

## Cost Optimization Tips

### IBM Cloud
1. Use **Code Engine** for auto-scaling (pay per use)
2. Choose **Lite tier** for databases during development
3. Use **Cloud Object Storage** instead of file storage
4. Enable **auto-scaling** based on traffic

### AWS
1. Use **Fargate Spot** for cost savings (up to 70% off)
2. Enable **S3 Intelligent-Tiering** for frontend assets
3. Use **RDS Reserved Instances** for production databases
4. Set up **Auto Scaling** policies
5. Use **CloudFront** for caching and reduced data transfer costs

### General
1. Implement **caching** (Redis/Memcached)
2. Use **CDN** for static assets
3. Enable **compression** (gzip)
4. Monitor and optimize **database queries**
5. Use **serverless functions** for infrequent tasks

---

## Quick Deployment Commands Summary

### IBM Cloud (Cloud Foundry)
```bash
# Backend
cd backend && mvn clean package
ibmcloud cf push

# Frontend
cd frontend && npm run build
ibmcloud cf push
```

### AWS (Elastic Beanstalk)
```bash
# Backend
cd backend && mvn clean package
eb create && eb deploy

# Frontend
cd frontend && npm run build
aws s3 sync build/ s3://your-bucket --acl public-read
```

---

## Troubleshooting

### Common Issues

1. **Port conflicts**: Ensure backend uses `${PORT}` environment variable
2. **Database connection**: Check security groups and connection strings
3. **CORS errors**: Update CORS configuration with production URLs
4. **Memory issues**: Increase instance memory allocation
5. **Build failures**: Check Java/Node versions match requirements

### Support Resources
- IBM Cloud Docs: https://cloud.ibm.com/docs
- AWS Documentation: https://docs.aws.amazon.com/
- Spring Boot on Cloud: https://spring.io/guides/gs/spring-boot-kubernetes/

---

## Next Steps

1. Set up **CI/CD pipeline** (GitHub Actions, Jenkins, or cloud-native tools)
2. Implement **automated testing** before deployment
3. Configure **backup and disaster recovery**
4. Set up **monitoring and alerting**
5. Implement **security best practices** (secrets management, WAF, etc.)

---

**Made with Bob** 🚀