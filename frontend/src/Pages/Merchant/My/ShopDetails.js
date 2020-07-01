import React from 'react';
import axios from 'axios';
import {Container, Form, Button} from 'react-bootstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faSpinner } from '@fortawesome/free-solid-svg-icons'
import ROUTES from '../../../routes';
import LocationInput from '../../../Components/LocationInput';

class ShopDetails extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
        pageLoading: false,
        shopName: this.props.user.shop.shopName,
        typeOfService: this.props.user.shop.typeOfService,
        addressLine1: this.props.user.shop.addressLine1,
        latitude: this.props.user.shop.latitude,
        longitude: this.props.user.shop.longitude,
        showLocationInput: false
    };
  }

  hideLocationInput = () => {
    this.setState({showLocationInput: false});
    
  }

  setShopLocation = ({addressLine1, latitude, longitude}) => {
    this.setState({addressLine1, latitude, longitude});
  }

  submitDetails = () => {
    this.setState({pageLoading: true});
    axios.post("https://speedy-anthem-217710.an.r.appspot.com/update/shop/", {
      merchantID: this.props.user.ID,
      shopID: this.props.user.shop.shopID,
      shopName: this.state.shopName,
      typeOfService: this.state.typeOfService,
      addressLine1: this.state.addressLine1,
      latitude: this.state.latitude,
      longitude: this.state.longitude      
    })
    .then(resp => {
      console.log(resp.data);
      if("shopID" in resp.data) return this.props.history.push(ROUTES.merchant.dashboard);
      else throw new Error(resp.data.error);
    })
    .catch(ex => {
      console.log(ex);
      this.setState({pageLoading: false});
    });
  }

  render() {
    return (
      <>
        <Container>
          <h3 className="h3 my-5">Seller Details</h3>
          {
              this.state.pageLoading 
              ?   <div className="text-center mt-5"><FontAwesomeIcon icon={faSpinner} size="3x" /></div>
              :   <>
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
                              <option value="Groceries">Groceries</option>
                              <option value="Garments">Garments</option>
                              <option value="Electronics">Electronics Repair</option>
                              <option value="Others">Others</option>
                          </Form.Control>
                      </Form.Group>
                      
                      <Form.Group controlId="shopLocation"  className="my-5">
                          <Form.Label>Where do you plan to sell?</Form.Label>
                          <Form.Control
                              type="text" 
                              placeholder="Type to search location" 
                              value={this.state.addressLine1} 
                              autoComplete="off"
                              style={{backgroundColor: '#fff'}}
                              readOnly
                              onClick={() => {this.setState({showLocationInput: true})}}
                          />
                      </Form.Group>
                      <LocationInput 
                        show={this.state.showLocationInput} 
                        onHide={this.hideLocationInput}
                        setLocation={this.setShopLocation} 
                        enforceSubmission={false}
                        />                      
                      <Button 
                          variant="primary" 
                          size="lg"
                          type="submit" 
                          block
                          className="fixedBottomBtn"
                          onClick={this.submitDetails}>
                          Finish
                      </Button>
                  </>
          }
        </Container>
        </>
    );
  }
}

export default ShopDetails;