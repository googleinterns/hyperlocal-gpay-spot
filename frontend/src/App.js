import React from 'react';
import { BrowserRouter as Router, Route, Switch } from 'react-router-dom';
import ROUTES from './routes';
import FrontScreen from './Pages/FrontScreen';
import NavBar from './NavBar';

// Customer Pages
import ViewShops from './Pages/Customer/ViewShops';

// Merchant Pages
import ShopDetails from './Pages/Merchant/Onboarding/ShopDetails';
import MyDashboard from './Pages/Merchant/My/Dashboard';

class App extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      type: null,
      latitude: null,
      longitude: null,
      user: {
        auth: false,
        idToken: null
      }
    }
  }

  setType = (type) => {
    this.setState({ type });
  }

  auth = (idToken) => {
    let user = {
      auth: true,
      idToken,
      ID: idToken.sub,
      name: idToken.given_name
    };
    this.setState({ user });
  }

  setShop = (shop) => {
    this.setState((state) => {
      let user = { shop, ...state.user }
      return { user };
    });
  }

  setLocation = () => {
    let microapps = window.microapps;
    microapps.getCurrentLocation().then(response => {
      let locationJson;
      locationJson = response.data;
      this.setState({
        latitude: locationJson.latitude,
        longitude: locationJson.longitude
      });
    }).catch(error => {
      console.error('Error while getting Location: ', error);
    });
  }

  render() {
    return (
      <>
        <NavBar />
        <Router >
          <Switch>
            <Route path="/" exact>
              <FrontScreen auth={this.auth} user={this.state.user} setShop={this.setShop} />
            </Route>
            <Route path="/shops/all">
              <ViewShops setLocation={this.setLocation} latitude={this.state.latitude} longitude={this.state.longitude} />
            </Route>
            <Route path={ROUTES.merchant.onboarding.shopInfo} render={(props) => <ShopDetails {...props} setShop={this.setShop} user={this.state.user} />} />
            <Route path={ROUTES.merchant.dashboard} render={(props) => <MyDashboard {...props} user={this.state.user} />} />
          </Switch>
        </Router>
      </>
    );
  }
}

export default App;