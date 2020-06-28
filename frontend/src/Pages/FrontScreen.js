import React from 'react';
import { Button } from 'react-bootstrap';
import Container from 'react-bootstrap/Container';
import Row from 'react-bootstrap/Row';
import { Link } from "react-router-dom";


class FrontScreen extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      "auth": false
    }
  }

  componentDidMount() {
    this.authenticate();
  }

  authenticate() {
    const microapps = window.microapps;
    const request = { nonce: 'nonce typically generated server-side' };
    microapps.getIdentity(request).then(response => {
      /* response: Concatenation of '.' separated base64 encoded JSON strings,
       * of which string at index 1 (0-based indexing) is the user identity.
       *
       * Line 29: Decoding the base64 string at index 1 and parsing it as JSON
       */
      const decoded = JSON.parse(atob(response.split('.')[1])); 
      decoded["auth"] = true;
      this.setState(decoded);
      console.log('GetIdentity response: ', decoded);
    }).catch(error => {
      console.error('An error occurred while fetching identity: ', error);
    });
  }

  render() {
    if (this.state.auth === false) {
      return <><p>Screen is Loading. Please authenticate</p></>;
    } else {
      return (
        <Container className="p-5 center">
          <Row className="mb-5">
            <Button color="#FD485B" block variant="dark">
              <Link to="/shops/all" className="btn btn-dark box">Avail a Service</Link>
            </Button>
          </Row>
          <Row>
            <Button variant="dark" block>
              <Link to="/shops/all" className="btn btn-dark box">Provide a Service</Link>
            </Button>
          </Row>
        </Container>
      );
    }
  }
}

export default FrontScreen;