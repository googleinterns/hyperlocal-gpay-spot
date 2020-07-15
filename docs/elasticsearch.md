# Setting Elasticsearch on a Compute Engine from Scratch

## Installing and running Elasticsearch
- Follow the instructions [here](https://www.elastic.co/guide/en/elasticsearch/reference/current/getting-started-install.html)
- Run it as a daemon

## Adding the Mapping
- Follow the link [here](https://www.elastic.co/guide/en/elasticsearch/reference/current/getting-started-install.html) and use the mapping in `cloudfunction/elasticSearchMapping.json`

## Allow Elasticsearch to accept requests on Internal IP
- Add the following settings in `config/elasticsearch.yml` file
    - network.host: 0.0.0.0
    - network.bind_host: 0
    - network.publish_host: 104.198.211.106
    - cluster.initial_master_nodes: ["104.198.211.106"]
- **NOTE**: `104.198.211.106` needs to be replaced with the public IP of the Compute Engine

**NOTE**: It's good to create all components in the same region for better latency