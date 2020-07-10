import React from 'react';
import {Form, Button} from 'react-bootstrap';
import LocationInput from './LocationInput';

class ShopDetailsInput extends React.Component {
    constructor(props) {
      super(props);
      this.state = {
          shopName: this.props.value.shopName,
          typeOfService: this.props.value.typeOfService,
          addressLine1: this.props.value.addressLine1,
          latitude: this.props.value.latitude,
          longitude: this.props.value.longitude,
          showLocationInput: false
      };
    }

    hideLocationInput = () => {
        this.setState({showLocationInput: false});        
    }

    setShopLocation = ({addressLine1, latitude, longitude}) => {
        this.setState({addressLine1, latitude, longitude});
    }

    setShopDetails = () => {
        this.props.setShopDetails({
            shopName: this.state.shopName,
            typeOfService: this.state.typeOfService,
            addressLine1: this.state.addressLine1,
            latitude: this.state.latitude,
            longitude: this.state.longitude
        });
    }
    
    render() {
        return (
            <>
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
                    onClick={this.setShopDetails}>
                    {this.props.submit}
                </Button>
            </>
        );
    }
}

ShopDetailsInput.defaultProps = {
    submit: "Update",
    value: {
        shopName: '',
        typeOfService: '',
        addressLine1: '',
        latitude: null,
        longitude: null
    }
};
export default ShopDetailsInput;
