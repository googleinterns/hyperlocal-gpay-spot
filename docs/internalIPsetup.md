# **Connecting to cloud components via Internal IP**

This document details the process used to connect cloud components with each other. Specifically connecting App Engine to Search Index and CloudSQL. And similarly connecting CloudFunction to Search Index

CloudSQL and SearchIndex (Elasticsearch run on a compute Engine) can have both internal IP as well as external IP. An external IP is visible outside the cloud environment whereas an internal IP is not accessible outside the cloud environment. 

In an ordinary case, one is able to connect via both Internal IP as well as External IP. 

However, it is not possible to connect to the search Index via external IP. To connect to the search Index via an external IP, we need to ensure that the Search Index server is able to listen to incoming requests on port 9200 and 9300. Doing so would require manually setting up a firewall rule to open these ports to HTTP requests. However, as per [policy](https://g3doc.corp.google.com/company/teams/eip-cloud/gce_enforcer/faq.md?cl=head) the firewall rule would be deleted later. 

To workaround this, we can connect via Internal IP address since connecting via an internal IP does not require a firewall rule to be manually created. A VPC connector has been used for this purpose.

Creating a connector: [Link](https://cloud.google.com/vpc/docs/configure-serverless-vpc-access) \
Connector set up Instructions: [AppEngine](https://cloud.google.com/appengine/docs/standard/python/connecting-vpc), [Cloud Function](https://cloud.google.com/functions/docs/networking/connecting-vpc)

**NOTE**: Connector must be in the same region as the AppEngine and Cloud Function (*Region is Asia-NorthEast1 for our project*)
