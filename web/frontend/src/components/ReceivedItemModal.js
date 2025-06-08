import React from 'react';
import Modal from 'react-modal';

// Set the app element for accessibility
Modal.setAppElement('#root');

const modalStyles = {
  content: {
    top: '50%',
    left: '50%',
    right: 'auto',
    bottom: 'auto',
    marginRight: '-50%',
    transform: 'translate(-50%, -50%)',
    maxWidth: '400px',
    width: '90%',
    padding: '20px',
    borderRadius: '8px',
    boxShadow: '0 4px 10px rgba(0,0,0,0.3)',
  },
};

const ReceivedItemModal = ({ isOpen, onRequestClose, item }) => {
  return (
    <Modal
      isOpen={isOpen}
      onRequestClose={onRequestClose}
      style={modalStyles}
      contentLabel="Received Clothing Item"
    >
      <h2>You received:</h2>
      <h3>{item?.name}</h3>
      {item?.imageUrl && (
        <img
          src={item.imageUrl}
          alt={item.name}
          style={{ maxWidth: '100%', height: 'auto', marginTop: '10px' }}
        />
      )}
      <button onClick={onRequestClose} style={{ marginTop: '20px' }}>
        Close
      </button>
    </Modal>
  );
};

export default ReceivedItemModal;
