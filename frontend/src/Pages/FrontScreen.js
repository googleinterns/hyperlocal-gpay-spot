import React from 'react';
import { Button } from 'react-bootstrap';
import Container from 'react-bootstrap/Container';
import Row from 'react-bootstrap/Row';
import { Link } from "react-router-dom";
import axios from 'axios';
import ROUTES from '../routes';
import { withRouter } from 'react-router';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faSpinner } from '@fortawesome/free-solid-svg-icons'

class FrontScreen extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      pageLoading: true,
      "user": {
        "auth": false
      }
    }
  }

  componentDidMount() {
    if(this.props.user.auth) this.setState({pageLoading: false});
    else this.authenticate();
  }

  componentDidUpdate(prevProps) {
    if(this.props.user.auth && !prevProps.user.auth) this.setState({pageLoading: false});
  }

  authenticate = async () => {
    const microapps = window.microapps;
    const request = { nonce: 'nonce typically generated server-side' };
    const encoded = await microapps.getIdentity(request);
    const decoded = JSON.parse(atob(encoded.split('.')[1]));
    console.log("Identity API response: ", decoded);
    this.setState(decoded);
    this.setState({
      "auth": true
    });
    this.props.auth(decoded);
  }

  setAsMerchant = async () => {
    console.log(this.props)
    this.setState({pageLoading: true});
    let merchantShops = await axios.get(ROUTES.api.get.shopsByMerchantID.replace('%b', this.props.user.ID));
    if(!merchantShops.data[0])
    {
        let phoneJWT = await window.microapps.getPhoneNumber();
        await axios.post('https://speedy-anthem-217710.an.r.appspot.com/api/insert/merchant', {
            merchantID: this.props.user.ID,
            merchantName: this.props.user.name,
            phoneJWT
        });
        this.props.history.push(ROUTES.merchant.onboarding.shopInfo);
    }
    else 
    {
      this.props.setShop(merchantShops.data[0]);
      this.props.history.push(ROUTES.merchant.dashboard);
    }
  }

  render() {
    return (
      this.state.pageLoading 
      ? <div className="text-center mt-5"><FontAwesomeIcon icon={faSpinner} size="3x" /></div>
      : <Container className="p-5 center">
        <Row className="mb-5">
          <Button color="#FD485B" block variant="dark">
            <Link to="/shops/all" className="btn btn-dark box">Avail a Service</Link>
          </Button>
        </Row>
        <Row>
          <Button onClick={this.setAsMerchant} variant="dark" block>
            <span className="btn btn-dark box">Provide a Service</span>
          </Button>
        </Row>
      </Container>
    );
  }
}

export default withRouter(FrontScreen);