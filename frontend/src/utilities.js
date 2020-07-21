import axios from 'axios';


// Helper class to make HTTP requests with X-Authorization header
class AuthHTTP {

    static getHeaderConfig({idToken}) {
        return {
            headers: {
                "X-Authorization": idToken
            }
        };
    }
    
    static async get(url) {
        const idToken = await window.microapps.getIdentity();
        return axios.get(url, AuthHTTP.getHeaderConfig({ idToken }));
    }
    
    static async post(url, data) {
        const idToken = await window.microapps.getIdentity();
        return axios.post(url, data, AuthHTTP.getHeaderConfig({ idToken }));
    }

    static async put(url, data) {
        const idToken = await window.microapps.getIdentity();
        return axios.put(url, data, AuthHTTP.getHeaderConfig({ idToken }));
    }

}

export default AuthHTTP;