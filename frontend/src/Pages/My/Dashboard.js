import React from 'react';
import {Container, Card, Button} from 'react-bootstrap';
import ROUTES from '../../routes';

class Dashboard extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      // merchantName: 'Arvind',
      // typeOfService: 'Groceries',
      // addressLine1: 'Sector 4, Ghansoli, Navi Mumbai',
      // shopName: 'Arvind Fruits Shop'
    };
  }

  render() {
    return (
      <Container>
        <h3 className="h3 my-5">Seller Dashboard</h3>
        <h5 className="h5 px-2">Hi, {this.props.user.name}!</h5>
        <Card className="mx-auto" style={{ marginTop: '20px', width: '90%', border: 'none' }}>
          <Card.Body>
            <Card.Text style={{ lineHeight: '30px' }}>
              You are selling <strong>{this.props.user.shop.typeOfService}</strong><br/>
              in <strong>{this.props.user.shop.addressLine1}</strong><br/>
              as <strong>{this.props.user.shop.shopName}</strong><br/>
            </Card.Text>
          </Card.Body>
          <Button 
            variant="dark" 
            className="rounded-0 mb-5 py-2" 
            style={{ 
              margin: '0px 10px 0px 10px', 
              width: 'calc(100%-20px)'
            }}
            onClick={() => this.props.history.push(ROUTES.merchant.shopInfo)}>
            Change
          </Button>
          <Button
            variant="primary"
            className="rounded-0 py-2" 
            style={{
              margin: '0px 10px 0px 10px', 
              width: 'calc(100%-20px)'
            }}
            onClick={() => this.props.history.push(ROUTES.merchant.catalog)}>
            View Catalog
          </Button>
        </Card>
      </Container>
    );
  }
}

export default Dashboard;