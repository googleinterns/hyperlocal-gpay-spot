import React from 'react';
import axios from 'axios';
import Container from 'react-bootstrap/Container';
import { Row, Col, Button } from 'react-bootstrap';
import Jumbotron from 'react-bootstrap/Jumbotron';
import Card from 'react-bootstrap/Card';
import Image from 'react-bootstrap/Image';
import ListGroup from 'react-bootstrap/ListGroup';

const IMG_URL = "https://secure.webtoolhub.com/static/resources/icons/set112/56bb7adb.png";
let URL_SHOP = "https://speedy-anthem-217710.an.r.appspot.com/api/shop";

class BuyerCatalog extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      shopID: props.match.params.shopid,
      shopFetched: false
    };
  }

  componentDidMount() {
    this.getShopDetails();
  }

  getShopDetails() {
    const shopID = this.state.shopID;
    axios.get(`${URL_SHOP}/${shopID}`)
      .then((response) => {
        this.setState(response.data);
        this.setState({
          shopFetched: true
        })
      });
  }

  render() {
    if (this.state.shopFetched === false) {
      this.getShopDetails();
      return (
        <Container className="p-0 ">
          <Jumbotron>
          </Jumbotron>
        </Container>
      );
    } else {
      return (
        <>
          <Container className="p-0 ">
            <Jumbotron>
              <Row className="p-0">
                <Col>
                  <h4>{this.state.shop.shopName}</h4>
                </Col>
                <Col>
                  <Button className="icon">
                    <a href={"tel:"+this.state.merchant.merchantPhone}>
                    <Image src={IMG_URL} className="photo" />
                    </a>
                  </Button>
                </Col>
              </Row>
            </Jumbotron>
          </Container>
          <Container>
            <ListGroup variant="flush" className="mt-4">
              {
                this.state.catalog.map(
                  function (catalogItem) {
                    return (
                      <Row className="p-2" key={catalogItem.serviceID}>
                        <Col>
                          <ListGroup.Item key={catalogItem.serviceID}>
                            <Image src={catalogItem.imageURL} thumbnail />
                            <Card style={{ backgroundColor: "#E3F2FD" }}>
                              <Card.Body>
                                <Card.Title>{catalogItem.serviceName}</Card.Title>
                                <Card.Text>
                                  {catalogItem.serviceDescription}
                                </Card.Text>
                              </Card.Body>
                            </Card>
                          </ListGroup.Item>
                        </Col>
                      </Row>
                    );
                  }
                )
              }
            </ListGroup>
          </Container>
        </>
      );
    }
  }
}

export default BuyerCatalog;