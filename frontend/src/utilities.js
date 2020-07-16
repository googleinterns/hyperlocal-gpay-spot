import axios from 'axios';

class AuthHTTP {

    static getHeaderConfig({idToken}) {
        return {
            headers: {
                "X-Authorization": idToken
            }
        };
    }
    
    static async get(url) {
        const idToken = await windows.microapps.getIdentity();
        return axios.get(url, getHeaderConfig({ idToken }));
    }
    
    static async post(url, data) {
        const idToken = await windows.microapps.getIdentity();
        return axios.post(url, data, getHeaderConfig({ idToken }));
    }

    static async put(url, data) {
        const idToken = await windows.microapps.getIdentity();
        return axios.put(url, data, getHeaderConfig({ idToken }));
    }

}

export default AuthHTTP;