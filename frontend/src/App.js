import React from 'react';
import { BrowserRouter as Router, Route, Switch } from 'react-router-dom';
import FrontScreen from './Pages/FrontScreen';
import NavBar from './NavBar';

class App extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      "type": null,
      "latitude": null,
      "longitude": null
    }
    this.setLocation = this.setLocation.bind(this)
  }

  setType(type) {
    this.setState({
      type: type,
      latitude: this.state.latitude,
      longitude: this.state.longitude
    });
  }

  setLocation() {
    let microapps = window.microapps;
    let locationJson;
    microapps.getCurrentLocation().then(response => {
      locationJson = response.data;
      this.setState({
        type: this.state.type,
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
              <FrontScreen />
            </Route>
          </Switch>
        </Router>
      </>
    );
  }
}

export default App;