const base = 'http://penguin.termina.linux.test:8080';

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
        }
    }
};

export default ROUTES;