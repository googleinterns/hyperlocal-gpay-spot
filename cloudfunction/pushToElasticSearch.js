/**
* Triggered from a message on a Cloud Pub/Sub topic.
*
* @param {!Object} event Event payload.
* @param {!Object} context Metadata for the event.
*/

const mysql = require('promise-mysql');
const axios = require('axios');
const SHOP_SELECT_STATEMENT = `SELECT MerchantID, ShopID, ShopName, Latitude, Longitude, AddressLine1, TypeOfService FROM Shops WHERE ShopID=?;`;
const MERCHANT_SELECT_STATEMENT = `SELECT MerchantName, MerchantPhone FROM Merchants WHERE MerchantID=?;`
const CATALOG_SELECT_STATEMENT = `SELECT ServiceName from Catalog WHERE ShopID = ?;`
const ELASTICSEARCH_SERVER_URL = 'http://10.128.0.13:9200'
const INDEX_NAME = "shops"
const SOCKET_PATH = "/cloudsql/speedy-anthem-217710:us-central1:hyperlocal";

let connection;

const dbConfig = {
  database: "hyperlocal",
  user: "root",
  socketPath: SOCKET_PATH
};

async function getShopDataByShopID(shopID) {
  const shopData = await connection.query(SHOP_SELECT_STATEMENT, [shopID]);
  return shopData;
}

async function getMerchantDataByMerchantID(merchantID) {
  const merchantData = await connection.query(MERCHANT_SELECT_STATEMENT, [merchantID]);
  return merchantData;
}

async function getCatalogByShopID(shopID) {
  const catalogItemsList = await connection.query(CATALOG_SELECT_STATEMENT, [shopID]);
  return catalogItemsList;
}

function createElasticSearchJson(shopData, merchantData, catalogItems) {
  const catalogItemNames = catalogItems.map((catalogItem) => {
    return catalogItem.ServiceName
  });

  const shopDataJson = {
    "shopname": shopData.ShopName,
    "typeofservice": shopData.TypeOfService,
    "shopid": shopData.ShopID,
    "pin": {
      "location": {
        "lat": shopData.Latitude,
        "lon": shopData.Longitude
      }
    },
    "merchantname": merchantData.MerchantName,
    "merchantphone": merchantData.MerchantPhone,
    "catalogitems": catalogItemNames
  }

  return shopDataJson;
}

async function pushToElasticsearch(shopDataJson) {

  const shopDataJsonString = JSON.stringify(shopDataJson);
  const config = {
    method: 'put',
    url: `${ELASTICSEARCH_SERVER_URL}/${INDEX_NAME}/_doc/${shopDataJson["shopid"]}`,
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
  connection = await mysql.createConnection(dbConfig);
  const shopData = await getShopDataByShopID(shopID);
  const merchantData = await getMerchantDataByMerchantID(shopData[0].MerchantID);
  const catalogItems = await getCatalogByShopID(shopID);
  shopDataJson = createElasticSearchJson(shopData[0], merchantData[0], catalogItems);
  await pushToElasticsearch(shopDataJson);
};
