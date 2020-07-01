import React from 'react';
import {Container} from 'react-bootstrap';

class ShopDetails extends React.Component {
  constructor(props) {
    super(props);
    this.state = {};
  }

  render() {
    return (
        <Container>
          <h3 className="h3 my-5">Seller Onboarding</h3>
        </Container>
    );
  }
}

export default ShopDetails;