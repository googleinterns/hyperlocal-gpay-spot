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
      const decoded = JSON.parse(atob(response.split('.')[1]));
      this.setState(decoded);
      this.setState({
        "auth": true
      });
      console.log('getIdentity response: ', decoded);
    }).catch(error => {
      console.error('An error occurred: ', error);
    });
  }

  render() {
    if (this.state.auth === false) {
      this.authenticate();
      return <> </>;
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