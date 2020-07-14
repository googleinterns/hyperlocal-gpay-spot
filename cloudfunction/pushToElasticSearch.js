/**
* Triggered from a message on a Cloud Pub/Sub topic.
*
* @param {!Object} event Event payload.
* @param {!Object} context Metadata for the event.
*/

const axios = require('axios');

const SEARCH_INDEX_URL = 'http://10.128.0.13:9200'
const INDEX_NAME = "shops"
const GET_SHOP_DATA_URL = 'http://speedy-anthem-217710.an.r.appspot.com/v1/shops/:shopid'

async function getShopDetailsByShopID(shopID) {
  const shopDetailsPromise = await axios.get(GET_SHOP_DATA_URL.replace(':shopid', shopID));
  return shopDetailsPromise.data;
}

function createElasticSearchJson(shopDetails, shopID) {
  const shopData = shopDetails["shop"];
  const merchantData = shopDetails["merchant"]
  const catalogItems = shopDetails["catalog"]
  const catalogItemNames = catalogItems.map((catalogItem) => {
    return catalogItem.serviceName
  });

  const shopDataJson = {
    "shopname": shopData.shopName,
    "typeofservice": shopData.typeOfService,
    "shopid": shopID,
    "pin": {
      "location": {
        "lat": shopData.latitude,
        "lon": shopData.longitude
      }
    },
    "merchantname": merchantData.merchantName,
    "merchantphone": merchantData.merchantPhone,
    "catalogitems": catalogItemNames
  }

  return shopDataJson;
}

async function pushToElasticsearch(shopDataJson) {

  const shopDataJsonString = JSON.stringify(shopDataJson);
  const config = {
    method: 'put',
    url: `${SEARCH_INDEX_URL}/${INDEX_NAME}/_doc/${shopDataJson["shopid"]}`,
    headers: {
      'Content-Type': 'application/json'
    },
    data: shopDataJsonString
  };

  axios(config)
    .then(function (response) {
      console.log(JSON.stringify(response.data));
    })
    .catch(function (error) {
      console.log(error);
    });
}

exports.elasticsearch = async (event, context) => {
  const shopID = Buffer.from(event.data, 'base64').toString()
  const shopDetails = await getShopDetailsByShopID(shopID);
  shopDataJson = createElasticSearchJson(shopDetails, shopID);
  await pushToElasticsearch(shopDataJson);
};
