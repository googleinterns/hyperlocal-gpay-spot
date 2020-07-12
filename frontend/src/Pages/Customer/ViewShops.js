import axios from 'axios';
import React from 'react';
import { Col, Button, Card, Container, Form, ListGroup } from 'react-bootstrap';
import { Link } from "react-router-dom";
import '../../App.css';
import LocationInput from '../../Components/LocationInput';
import ROUTES from '../../routes';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faSpinner } from '@fortawesome/free-solid-svg-icons';

class ViewShops extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
      shops: [],
      pageLoading: true,
      searchQuery: "",
      queryRadius: "",
      showModal: true
    }
  }

  search = async () => {

    this.setState({ pageLoading: true });

    let requestParams = {};
    requestParams.latitude = this.props.latitude;
    requestParams.longitude = this.props.longitude;

    if (this.state.searchQuery !== "") {
      requestParams.query = this.state.searchQuery;
    }

    if (this.state.queryRadius !== "") {
      requestParams.queryRadius = this.state.queryRadius;
    }

    const config = {
      method: 'get',
      url: ROUTES.v1.get.index.search,
      headers: {},
      params: requestParams
    };

    const axiosResponse = await axios(config);
    const shopDetailsList = axiosResponse.data;

    this.setState({
      "shops": shopDetailsList,
      searchQuery: "",
      queryRadius: "",
      pageLoading: false
    })

  }

  searchBoxUpdateHandler = (e) => {
    this.setState({ searchQuery: e.target.value });
  }

  onHide = () => {
    this.setState({
      showModal: false
    });
  }

  convertDegreeToRadians = (latitudeInDegree) => {
    return Math.PI * latitudeInDegree / 180.0
  }

  // Calculate distance between two points using Haversine formula
  getDistanceToShop = (shop) => {
    // lambda - longitude
    // phi - latitude
    const EARTH_RADIUS_IN_KM = 6371;

    const firstPointLatitude = shop["shop"]["latitude"];
    const firstPointLongitude = shop["shop"]["longitude"];
    const secondPointLatitude = this.props.latitude;
    const secondPointLongitude = this.props.longitude;

    const firstPointLatitudeInRadian = this.convertDegreeToRadians(firstPointLatitude);
    const secondPointLatitudeInRadian = this.convertDegreeToRadians(secondPointLatitude);

    const deltaLatitude = Math.abs(secondPointLatitude - firstPointLatitude);
    const deltaLongitude = Math.abs(secondPointLongitude - firstPointLongitude);

    const deltaLatitudeInRadian = this.convertDegreeToRadians(deltaLatitude);
    const deltaLongitudeInRadian = this.convertDegreeToRadians(deltaLongitude);

    const centralAngleInRadian = 2 * Math.asin(
      Math.sqrt(Math.sin(deltaLatitudeInRadian / 2) * Math.sin(deltaLatitudeInRadian / 2)
        + Math.cos(firstPointLatitudeInRadian) * Math.cos(secondPointLatitudeInRadian)
        * Math.sin(deltaLongitudeInRadian / 2) * Math.sin(deltaLongitudeInRadian / 2)
      )
    );

    // arc_length = radius * angle subtended
    return (centralAngleInRadian * EARTH_RADIUS_IN_KM).toFixed(2);
  }

  componentDidUpdate(prevProps) {
    if (this.props.latitude !== prevProps.latitude || (this.props.longitude !== prevProps.longitude)) {
      this.search();
    }
  }

  componentDidMount() {
    if (this.props.latitude !== null || (this.props.longitude !== null)) {
      this.search();
    }
  }

  render() {
    if (this.props.latitude == null || this.props.longitude == null) {
      return (
        <LocationInput setLocation={this.props.setLocation} show={this.state.showModal} onHide={this.onHide} />
      );
    } else {
      return (
        this.state.pageLoading
          ? <div className="text-center mt-5"><FontAwesomeIcon icon={faSpinner} size="3x" /></div>
          : <Container className="mt-1 p-3">
            <Form onSubmit={(e) => { e.preventDefault(); this.search(); }}>
              <Form.Row className="align-items-center">
                <Col xs={7}>
                  <Form.Control
                    placeholder="Search Nearby"
                    autoComplete="off"
                    onChange={this.searchBoxUpdateHandler}
                  />
                </Col>
                <Col xs={3}>
                  <Form.Control
                    placeholder="3km"
                    aria-label="distance"
                    onChange={e => this.setState({ queryRadius: e.target.value })}
                  />
                </Col>
                <Col xs={2}>
                  <Button variant="success" onClick={this.search}>
                    Go
                </Button>
                </Col>
              </Form.Row>
            </Form>

            <ListGroup variant="flush" className="mt-4">
              {
                this.state.shops.map(shop => {
                  const shopDistanceInKM = this.getDistanceToShop(shop);
                  return (
                    <ListGroup.Item key={shop["shop"]["shopID"]}>
                      <Card className="mb-4" style={{ backgroundColor: "#E3F2FD" }}>
                        <Card.Body>
                          <Card.Title>{shop["shop"]["shopName"]}</Card.Title>
                          <Card.Text>
                            <i><b>{shopDistanceInKM} km away</b></i>
                            <br />
                            <b />
                            <b>Sold By: </b>{shop["merchant"]["merchantName"]}
                            <br />
                            <b>At: </b>{shop["shop"]["addressLine1"]}
                            <br />
                            <b>Reach out at: </b>{shop["merchant"]["merchantPhone"]}
                          </Card.Text>
                          <Button variant="info">
                            <Link
                              to={{
                                pathname: ROUTES.customer.catalog.replace(':shopid', shop["shop"]["shopID"]),
                              }} className="btn btn-info box">
                              View Catalog
                          </Link>
                          </Button>
                        </Card.Body>
                      </Card>
                    </ListGroup.Item>
                  )
                })
              }
            </ListGroup>
          </Container>
      );
    }
  }
}

export default ViewShops;
