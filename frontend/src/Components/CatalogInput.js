import React from 'react';
import { Button, Card, Row, Col, Modal, Form } from 'react-bootstrap';
import { Container as FABContainer, Button as FAButton } from 'react-floating-action-button'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faPlus, faTimes } from '@fortawesome/free-solid-svg-icons'

class CatalogInput extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
        showAddForm: false,
        addForm: {
          'serviceName': '',
          'serviceDescription': '',
          'serviceImageURL': '',
        },
        showEditForm: false,
        editForm: {
            'serviceName': '',
            'serviceDescription': '',
            'serviceImageURL': '',  
        },
        catalog : JSON.parse(JSON.stringify(this.props.value)), // Deep copy
        initialCatalog: this.props.value
    };
  }

  setCatalog = () => {
    let initialCatalog = this.state.initialCatalog;
    let currentCatalog = this.state.catalog;

    let itemsToDelete = [];
    for(let i = 0; i < initialCatalog.length; i++)
    {
        let curItemKey = initialCatalog[i].key;
        let curItemKeyExists = false;
        for(let j = 0; j < currentCatalog.length; j++)
        {
            if(currentCatalog[j].key === curItemKey)
            {
                curItemKeyExists = true;
                break;
            }
        }
        if(!curItemKeyExists)
            itemsToDelete.push({serviceID: initialCatalog[i].serviceID});
    }

    let itemsToCreate = [];
    for(let i = 0; i < currentCatalog.length; i++)
    {
        if(!("serviceID" in currentCatalog[i]))
        {
            let itemToCreate = currentCatalog[i];
            itemsToCreate.push({
                serviceName: itemToCreate.serviceName,
                serviceDescription: itemToCreate.serviceDescription,
                imageURL: itemToCreate.serviceImageURL
            });
            
        }
    }

    let itemsToUpdate = [];
    for(let i = 0; i < initialCatalog.length; i++)
    {
        let curItemKey = initialCatalog[i].key;
        for(let j = 0; j < currentCatalog.length; j++)
        {
            if(currentCatalog[j].key !== curItemKey) continue;
            let itemBefore = initialCatalog[i];
            let itemAfter = currentCatalog[j];
            if( itemBefore.serviceName !== itemAfter.serviceName ||
                itemBefore.serviceDescription !== itemAfter.serviceDescription ||
                itemBefore.serviceImageURL !== itemAfter.serviceImageURL)
            {
                itemsToUpdate.push({
                    serviceID: itemAfter.serviceID,
                    serviceName: itemAfter.serviceName,
                    serviceDescription: itemAfter.serviceDescription,
                    imageURL: itemAfter.serviceImageURL        
                });
            }
            break;
        }
    }

    this.props.setCatalog({
        itemsToCreate,
        itemsToUpdate,
        itemsToDelete
    });

  }

  addProduct = () => {
    this.setState(state => {
        let itemsList = state.catalog;
        let newItem = {
            serviceName: state.addForm.serviceName,
            serviceDescription: state.addForm.serviceDescription,
            serviceImageURL: state.addForm.serviceImageURL,
            key: Date.now().toString()
        };
        itemsList.push(newItem);
        this.hideAddForm();
        return {catalog: itemsList};
    });
  }

  deleteProduct = (productKey) => {
    let existingItemsList = this.state.catalog;
    let newItemsList = existingItemsList.filter(item => item.key !== productKey);
    this.setState({catalog: newItemsList});
    return;
  }

  addNewServiceImageURL = (e) => {
    this.setState(state => {
        let addForm = state.addForm;
        addForm.serviceImageURL = e.target.value;
        return { addForm };
    });
  }

  addNewServiceDescription = (e) => {
    this.setState(state => {
        let addForm = state.addForm;
        addForm.serviceDescription = e.target.value;
        return { addForm };
    });
  }

  addNewServiceName = (e) => {
    this.setState(state => {
        let addForm = state.addForm;
        addForm.serviceName = e.target.value;
        return { addForm };
    });
  }

  showAddForm = () => {
    this.setState({showAddForm: true});
  }

  hideAddForm = () => {
    this.setState({showAddForm: false, addForm: {serviceName: '', serviceDescription: '', serviceImageURL: ''}});
  }
  
  render() {
    let products = this.state.catalog.map((product) => {
        return (
            <React.Fragment key={product.key}>
            <Row>
                <Col xs={4}>
                    <Card.Img variant="top" src={product.serviceImageURL} />
                </Col>
                <Col xs={8}>
                    <h6>
                      {product.serviceName}
                      <button
                        className="pull-right"
                        onClick={e => {e.preventDefault(); this.deleteProduct(product.key)}}
                        style={{
                          marginRight: '10px',
                          color: 'white',
                          backgroundColor: '#d64941',
                          padding: '0px 4px',
                          borderRadius: '3px',
                          border: '0px',
                        }}
                      >
                        <FontAwesomeIcon icon={faTimes} size="sm" />
                      </button>
                    </h6>
                    <p className="small" style={{marginBottom: '0px'}}>{product.serviceDescription}</p>
                </Col>
            </Row>
            <hr />
            </React.Fragment>
        );
    });

    return (
      <>
        {
            this.state.catalog.length === 0
            ? <p className="text-center my-5">There's nothing here. Add something by clicking the Plus button below!</p>
            : <>{products}</>
        }
        <Modal
            show={this.state.showAddForm}
            onHide={this.hideAddForm}
        >
            <Modal.Header closeButton>
            <Modal.Title>Add Product/Service</Modal.Title>
            </Modal.Header>
            <Modal.Body>
            <Form onSubmit={e => {e.preventDefault(); return this.addProduct();}}>
                <Form.Group controlId="addServiceName">
                <Form.Label>What are you selling?</Form.Label>
                <Form.Control type="text" placeholder="Dasheri Mangos" onChange={this.addNewServiceName} autoComplete="off" />
                </Form.Group>

                <Form.Group controlId="addServiceDescription">
                <Form.Label>Brief description</Form.Label>
                <Form.Control as="textarea" rows="2" placeholder="Dasheri mangos fresh off the market!" onChange={this.addNewServiceDescription} />
                </Form.Group>

                <Form.Group controlId="addServiceImageURL">
                <Form.Label>Image URL</Form.Label>
                <Form.Control type="text" placeholder="https://example.com/mangos.jpg" onChange={this.addNewServiceImageURL} autoComplete="off" />
                </Form.Group>
            </Form>

            </Modal.Body>
            <Modal.Footer>
            <Button variant="secondary" onClick={this.hideAddForm}>
                Cancel
            </Button>
            <Button variant="primary" onClick={this.addProduct}>Add</Button>
            </Modal.Footer>
        </Modal>
        <FABContainer>
            <FAButton
                styles={{
                    backgroundColor: '#343a40', 
                    color: '#fff',
                    marginBottom: '5px',
                    marginRight: '-2px',
                }}
                tooltip="Add products!"
                onClick={this.showAddForm}>
                    <FontAwesomeIcon icon={faPlus} size="lg" />
            </FAButton>
        </FABContainer>
        <Button 
            onClick={this.setCatalog}
            variant="primary" 
            size="lg"
            block
            className="fixedBottomBtn">
            { this.props.submit }
        </Button>
      </>
    );
  }
}

CatalogInput.defaultProps = {
  submit: "Submit",
  value: []
};
export default CatalogInput;
