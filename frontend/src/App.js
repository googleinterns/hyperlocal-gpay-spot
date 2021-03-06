import React from 'react';
import { BrowserRouter as Router, Route, Switch } from 'react-router-dom';
import ROUTES from './routes';
import FrontScreen from './Pages/FrontScreen';
import NavBar from './NavBar';

// Customer Pages
import ViewShops from './Pages/Customer/ViewShops';
import BuyerCatalog from './Pages/Customer/BuyerCatalog';

// Merchant Pages
import OnboardingShopDetails from './Pages/Merchant/Onboarding/ShopDetails';
import OnboardingCatalog from './Pages/Merchant/Onboarding/Catalog';
import MyDashboard from './Pages/Merchant/My/Dashboard';
import MyShopDetails from './Pages/Merchant/My/ShopDetails';
import MyCatalog from './Pages/Merchant/My/Catalog';


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
      let user = { ...state.user, shop }
      return { user };
    });
  }

  setLocation = ({latitude, longitude}) => {
    this.setState({
      latitude,
      longitude
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
            <Route path={ROUTES.customer.shopsList}>
              <ViewShops setLocation={this.setLocation} latitude={this.state.latitude} longitude={this.state.longitude} />
            </Route>
            <Route path={ROUTES.customer.catalog} component={BuyerCatalog} />
            <Route path={ROUTES.merchant.onboarding.shopInfo} render={(props) => <OnboardingShopDetails {...props} setShop={this.setShop} user={this.state.user} />} />
            <Route path={ROUTES.merchant.onboarding.catalog} render={(props) => <OnboardingCatalog {...props} user={this.state.user} />} />
            <Route path={ROUTES.merchant.dashboard} render={(props) => <MyDashboard {...props} user={this.state.user} />} />
            <Route path={ROUTES.merchant.shopInfo} render={(props) => <MyShopDetails {...props} setShop={this.setShop} user={this.state.user} />} />
            <Route path={ROUTES.merchant.catalog} render={(props) => <MyCatalog {...props} user={this.state.user} />} />
         </Switch>
        </Router>
      </>
    );
  }
}

export default App;
