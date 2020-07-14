# Manual steps in Project required

## Cloud SQL database
- Create a MySQL instance following the instructions [here](https://cloud.google.com/sql/docs/mysql/create-instance)
    - **NOTE**: CloudSQL instance should have private IP to enable connection with AppEngine
- Create a database by going to the cloudSQL console in GCP
- Import the CloudSQL tables into the database through console 

## ElasticSearch Index 
- Please see `elasticsearch.md` under docs

## Create a VPC connector
- Please see `internalIPsetup.md`

## Creating PubSub topic
- Create a topic following [this](https://cloud.google.com/pubsub/docs/quickstart-py-mac) link

## CloudFunction
- Create a cloudFunction triggered on Publish to pubSub by following the guide [here](https://cloud.google.com/functions/docs/quickstart-nodejs)
- Make sure to set the trigger to the pubSub topic created earlier
- Make sure to create it in the same region as the VPC connector
- Add the VPC connector 
- Create the function
- Edit and copy Paste the code mentioning all dependencies in `package.json`