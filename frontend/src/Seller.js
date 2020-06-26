import React from 'react';
import axios from 'axios';
import { Container, Form, Button } from 'react-bootstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faSpinner } from '@fortawesome/free-solid-svg-icons'
import ROUTES from '../../routes';

class ShopDetails extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      pageLoading: true,
      merchantID: 2,
      shopID: null,
      shopName: '',
      typeOfService: '',
      addressLine1: '',
      latitude: 23.55555,
      longitude: 85.846925
    };
  }

  componentDidMount() {
    return this.resetAndReloadDetails();
  }

  resetAndReloadDetails = () => {
    return axios.get("https://speedy-anthem-217710.an.r.appspot.com/api/merchant/" + this.state.merchantID + "/shops")
      .then(res => {
        console.log(res.data);
        let shopDetails = res.data[0];
        this.setState({
          shopID: shopDetails.shopID,
          shopName: shopDetails.shopName,
          typeOfService: shopDetails.typeOfService,
          addressLine1: shopDetails.addressLine1,
          pageLoading: false
        });
        return;
      })
      .catch(err => {
        console.log(err);
        alert("Whoops, something went wrong. Trying again...");
        return this.resetAndReloadDetails();
      });
  }


  submitDetails = () => {
    axios.post("https://speedy-anthem-217710.an.r.appspot.com/update/shop/", {
      merchantID: this.state.merchantID,
      shopID: this.state.shopID,
      shopName: this.state.shopName,
      typeOfService: this.state.typeOfService,
      addressLine1: this.state.addressLine1,
      latitude: this.state.latitude,
      longitude: this.state.longitude
    })
      .then(resp => {
        console.log(resp.data);
        if ("shopID" in resp.data) return this.props.history.push(ROUTES.merchant.dashboard);
        else alert(resp.data.error);
      })
      .catch(ex => {
        console.log(ex);
      });
  }

  render() {
    return (
      <Container>
        <h3 className="h3 my-5">Seller Details</h3>
        {
          <Form onSubmit={(e) => { e.preventDefault(); this.submitDetails(); }}>
            <Form.Group controlId="shopName" className="my-5">
              <Form.Label>What should we call your shop?</Form.Label>
              <Form.Control
                type="text"
                placeholder="Arvind Fruits Shop"
                value={this.state.shopName}
                onChange={e => this.setState({ shopName: e.target.value })}
                autoComplete="off"
                autoFocus
              />
            </Form.Group>

            <Form.Group controlId="shopCategory" className="my-5">
              <Form.Label>What do you plan to sell?</Form.Label>
              <Form.Control value={this.state.typeOfService} onChange={e => this.setState({ typeOfService: e.target.value })} as="select">
                <option disabled value=""> -- Select product/service -- </option>
                <option value="food">Groceries</option>
                <option value="garments">Garments</option>
                <option value="electronics">Electronics Repair</option>
                <option value="others">Others</option>
              </Form.Control>
            </Form.Group>

            <Form.Group controlId="shopLocation" className="my-5">
              <Form.Label>Where do you plan to sell?</Form.Label>
              <Form.Control
                type="text"
                placeholder="Type to search location"
                value={this.state.addressLine1}
                autoComplete="off"
                onChange={e => this.setState({ addressLine1: e.target.value })}
              />
            </Form.Group>

            <Button
              variant="primary"
              size="lg"
              type="submit"
              block
              className="fixedBottomBtn"
              onClick={this.submitDetails}>
              Finish
            </Button>
          </Form>
        }
      </Container>
    );
  }
}

export default ShopDetails;