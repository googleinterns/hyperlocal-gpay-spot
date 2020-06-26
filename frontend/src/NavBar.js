import React from 'react';
import {Navbar, Nav} from 'react-bootstrap';

class NavBar extends React.Component {
  constructor(props) {
    super(props);
    this.state = {};
  }

  render() {
    return (
        <Navbar collapseOnSelect expand="lg" bg="dark" variant="dark">
            <Navbar.Brand href="#home">Microapp</Navbar.Brand>
            <Navbar.Toggle aria-controls="responsive-navbar-nav" />
            <Navbar.Collapse id="responsive-navbar-nav">
                <Nav className="mr-auto">
                    <Nav.Link href="/consumer">Switch as Consumer</Nav.Link>
                    <Nav.Link href="/dashboard">Seller Dashboard</Nav.Link>
                    <Nav.Link href="/logout">Log out</Nav.Link>
                </Nav>
            </Navbar.Collapse>
            
        </Navbar>
    );
  }
}

export default NavBar;