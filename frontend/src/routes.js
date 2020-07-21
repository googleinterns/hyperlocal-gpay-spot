const base = 'https://speedy-anthem-217710.an.r.appspot.com';

const ROUTES = {
    'merchant': {
        'dashboard' : '/my/dashboard',
        'catalog': '/my/catalog',
        'shopInfo': '/my/shop-info',
        'onboarding': {
            'shopInfo': '/onboarding/shop-info',
            'catalog': '/onboarding/catalog'
        }
    },
    'v1': {
        'get': {
            'shopsByMerchantID': base + '/v1/merchants/%b/shops',
            'index': {
              'search': base + '/v1/shops'
            },
            'shopByShopID': base + '/v1/shops/%b'
        },
        'post': {
            'insertMerchant': base + '/v1/merchants',
            'insertShop': base + '/v1/merchants/:merchantID/shops',
            'updateCatalog': base + '/v1/merchants/:merchantID/shops/:shopID/catalog:batchUpdate'
        },
        'put': {
            'updateShop': base + '/v1/merchants/:merchantID/shops/:shopID'
        }
    },
    'customer': {
      'catalog': '/buyercatalog/:shopid',
      'shopsList': '/shops/all'
    }
};

export default ROUTES;
