# Hyperlocal Shopping Spot 

**This is not an official Google product.** 

## Introduction
This is the source code for Hyperlocal Shopping Spot App hosted on Google Pay.
For more details about Spot Platform, please visit [this](https://developers.google.com/pay/spot)

For design doc please visit [go/hyperlocal-shopping](go/hyperlocal-shopping) (Only accessible to google.com accounts)

Please read the following documentation files in this order
- `manualsteps.md`
- `elasticsearch.md`
- `internalIPsetup.md`

The project is hosted on App Engine on Google Cloud Platform. 

## Project Structure
---

### app.yaml
The app.yaml file in `src/main/appengine/app.yaml` is the configuration file for the app engine deployment. The code is written in Java 11 runtime. 

### cloudbuild.yaml
Build and deployment to App Engine happens automatically whenever code is pushed to master branch. This is done via a build trigger installed via Cloud Build in GCP. 

### pom.xml 
All dependencies for the backend are listed in this file. Plugin for integration with frontend is also specified there.

### schema directory
Schema used in cloudSQL MySQL database tables (Merchants, Shops and Catalog) is specified there

### cloud directory
Document Mapping used for Search Index, Sample query sent for search and the CloudFunction code is added there
