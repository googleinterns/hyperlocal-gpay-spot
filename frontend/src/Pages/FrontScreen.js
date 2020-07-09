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
    this.authenticate();
  }

  componentDidUpdate(prevProps) {
    if(this.props.user.auth && !prevProps.user.auth) {
      this.setState({pageLoading: false});
    }
  }

  authenticate = async() => {
    const microapps = window.microapps;
    const request = { nonce: 'nonce typically generated server-side' };
    microapps.getIdentity(request).then(response => {
      /* response: Concatenation of '.' separated base64 encoded JSON strings,
       * of which string at index 1 (0-based indexing) is the user identity.
       *
       * Line below: Decoding the base64 string at index 1 and parsing it as JSON
       */
      const decoded = JSON.parse(atob(response.split('.')[1])); 
      console.log('GetIdentity response: ', decoded);
      this.props.auth(decoded);
      this.setState({
        "auth": true
      });
    }).catch(error => {
      console.error('An error occurred while fetching identity: ', error);
    });
  }

  setAsMerchant = async () => {
    this.setState({pageLoading: true});
    let merchantShops = await axios.get(ROUTES.api.get.shopsByMerchantID.replace('%b', this.props.user.ID));
    if(!merchantShops.data[0])
    {
        await axios.post(ROUTES.api.post.insertMerchant, {
            merchantID: this.props.user.ID,
            merchantName: this.props.user.name,
            merchantPhone: "0123456789" // TODO: Implement Phone Number API
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
            <Link to={ROUTES.customer.shopsList} className="btn btn-dark box">Avail a Service</Link>
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