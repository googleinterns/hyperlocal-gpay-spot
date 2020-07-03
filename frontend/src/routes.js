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
    'api': {
        'get':{
            'shopsByMerchantID': base+'/api/merchant/%b/shops',
            'index': {
              'search': base+'/api/query/elastic',
              'browse': base+'/api/browse/elastic'
            }
        },
        'post':{
            'insertMerchant': base+'/api/insert/merchant'
        }
    },
    'customer': {
      'catalog': '/buyercatalog/',
      'shopsList': '/shops/all'
    }
};

export default ROUTES;