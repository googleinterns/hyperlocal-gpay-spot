import React from 'react';
import { BrowserRouter as Router, Route, Switch } from 'react-router-dom';
import ROUTES from './routes';
import FrontScreen from './FrontScreen';
import NavBar from './NavBar';
import HomePage from './Pages/HomePage';

// Customer Pages
import ViewShops from './ViewShops';
import BuyerCatalog from './BuyerCatalog';

// Merchant Pages
import ShopDetails from './Pages/Onboarding/ShopDetails';
import OnboardingCatalog from './Pages/Onboarding/Catalog';
import MyDashboard from './Pages/My/Dashboard';
import MyCatalog from './Pages/My/Catalog';
import MyShopDetails from './Pages/My/ShopDetails';

class App extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      "type": null,
      "latitude": null,
      "longitude": null,
      user: {
        auth: false,
        idToken: null
      }
    }
  }

  setType = (type) => {
    this.setState({
      type: type,
      latitude: this.state.latitude,
      longitude: this.state.longitude
    });
  }

  auth = (idToken) => {
    let user = {
      auth: true,
      idToken,
      ID: idToken.sub,
      name: idToken.given_name
    };
    this.setState({user});
  }

  setShop = (shop) => {
    this.setState((state) => {
      let user = {shop, ...state.user}
      return {user};
    });
  }

  /*
  TODO: REPLACE WITH RESPONSE FROM THE LOCATION SPOT API ONCE THAT THING IS SETUP
  */
  setLocation = (locationJson) => {
    // let locationJson = {
    //   "latitude": 24.54,
    //   "longitude": 24.56
    // };
    this.setState({
      type: this.state.type,
      latitude: locationJson.latitude,
      longitude: locationJson.longitude
    });
    console.log("state in set location app.js:", this.state);
  }

  render() {
    return (
      <>
        <NavBar />
        <Router >
          <Switch>
            <Route path="/home">
              <HomePage />
            </Route>
            <Route path="/shops/all">
              <ViewShops setLocation={this.setLocation} latitude={this.state.latitude} longitude={this.state.longitude} />
            </Route>
            <Route path="/" exact>
              <FrontScreen auth={this.auth} user={this.state.user} setShop={this.setShop} />
            </Route>
            <Route path="/buyercatalog/:shopid" component={BuyerCatalog} />

            <Route path={ROUTES.merchant.onboarding.shopInfo} render={(props) => <ShopDetails {...props} setShop={this.setShop} user={this.state.user} />} />
            <Route path={ROUTES.merchant.onboarding.catalog} render={(props) => <OnboardingCatalog {...props} user={this.state.user} />} />
            <Route path={ROUTES.merchant.dashboard} render={(props) => <MyDashboard {...props} user={this.state.user} />} />
            <Route path={ROUTES.merchant.catalog} render={(props) => <MyCatalog {...props} user={this.state.user} />} />
            <Route path={ROUTES.merchant.shopInfo} render={(props) => <MyShopDetails {...props} user={this.state.user} />} />
          </Switch>
        </Router>
      </>
    );
  }
}

export default App;