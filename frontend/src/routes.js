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
            'shopsByMerchantID': base+'/api/merchant/%b/shops'
        },
        'post':{
            'insertMerchant': base+'/api/insert/merchant'
        }
    }
};

export default ROUTES;