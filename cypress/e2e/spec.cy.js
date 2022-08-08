describe('empty spec', () => {
  it('Load demo', () => {
    cy.visit('http://localhost:12345')
  })

  it('String', () => {
    cy.get('.String').within(($form) => {
      cy.get('input').clear().type('hello')
      cy.get('.value').should('have.text', 'hello')
      cy.get('input').clear().type('moon')
      cy.get('.value').should('have.text', 'moon')
    })
  })

  it('Int', () => {
    cy.get('.Int').within(($form) => {
      cy.get('input').clear().type('7')
      cy.get('.value').should('have.text', '7')
      cy.get('input').clear().type('3')
      cy.get('.value').should('have.text', '3')
    })
  })

  it('Double', () => {
    cy.get('.Double').within(($form) => {
      cy.get('input').clear().type('2.3')
      cy.get('.value').should('have.text', '2.3')
      cy.get('input').clear().type('1e+230')
      cy.get('.value').should('have.text', '1e+230')
    })
  })

  it('Long', () => {
    cy.get('.Long').within(($form) => {
      cy.get('input').clear().type('10000000000000')
      cy.get('.value').should('have.text', '10000000000000')
      cy.get('input').clear().type('22222222222222')
      cy.get('.value').should('have.text', '22222222222222')
    })
  })

  it('Boolean', () => {
    cy.get('.Boolean').within(($form) => {
      cy.get('input').check()
      cy.get('.value').should('have.text', 'true')
      cy.get('input').uncheck()
      cy.get('.value').should('have.text', 'false')
    })
  })

  it('Option[Int]', () => {
    cy.get('.Option\\[Int\\]').within(($form) => {
      cy.get('input[type="checkbox"]').check()
      cy.get('.value').should('have.text', 'Some(0)')
      cy.get('input[type="text"]').clear().type('13')
      cy.get('.value').should('have.text', 'Some(13)')
      cy.get('input[type="checkbox"]').uncheck()
      cy.get('.value').should('have.text', 'None')
    })
  })

  it('Seq[Int]', () => {
    cy.get('.Seq\\[Int\\]').within(($form) => {
      cy.contains('button','add').click()
      cy.get('.value').should('have.text', 'List(0)')
      cy.get('> div:nth-child(2) > div:nth-child(1) input[type="text"]').clear().type('13')
      cy.get('.value').should('have.text', 'List(13)')
      cy.contains('button','add').click()
      cy.get('> div:nth-child(2) > div:nth-child(2) input[type="text"]').clear().type('4')
      cy.get('.value').should('have.text', 'List(13, 4)')
      cy.get('> div:nth-child(2) > div:nth-child(1) input[type="text"]').clear().type('14')
      cy.get('> div:nth-child(2) > div:nth-child(2) input[type="text"]').clear().type('5')
      cy.get('.value').should('have.text', 'List(14, 5)')
      cy.get('> div:nth-child(2) > div:nth-child(2) input[type="text"]').clear().type('5')
      cy.get('> div:nth-child(2) > div:nth-child(1)').contains('button', 'remove').click()
      cy.get('.value').should('have.text', 'List(5)')
      cy.get('> div:nth-child(2) > div:nth-child(1)').contains('button', 'remove').click()
      cy.get('.value').should('have.text', 'List()')
    })
  })

  it('Tuple', () => {
    cy.get('.Tuple').within(($form) => {
      cy.contains('tr', '_1:').find('input[type="text"]').clear().type('8')
      cy.get('.value').should('have.text', '(8,,None)')
      cy.contains('tr', '_2:').find('input[type="text"]').clear().type('Wurst')
      cy.get('.value').should('have.text', '(8,Wurst,None)')
      cy.contains('tr', '_3:').find('input[type="checkbox"]').check()
      cy.contains('tr', '_3:').find('input[type="text"]').clear().type('17')
      cy.get('.value').should('have.text', '(8,Wurst,Some(17))')
    })
  })

  it('Person (case class)', () => {
    cy.get('.Person').within(($form) => {
      cy.contains('tr', 'name:').find('input[type="text"]').clear().type('Klaus')
      cy.get('.value').should('have.text', 'Person(Klaus,5)') // test default value
      cy.contains('tr', 'age:').find('input[type="text"]').clear().type('7')
      cy.get('.value').should('have.text', 'Person(Klaus,7)')
    })
  })

  it('Pet (sealed trait)', () => {
    cy.get('.Pet').within(($form) => {
      cy.contains('tr', 'name:').find('input[type="text"]').clear().type('Tiger')
      cy.get('.value').should('have.text', 'Cat(Tiger,4)') // test default value
      cy.get('select').select('Dog')
      cy.get('.value').should('have.text', 'Dog(,true)') // test default value
    })
  })

  it('Tree (recursive case class)', () => {
    cy.get('.Tree').within(($form) => {
      cy.get('.value').should('have.text', 'Tree(2,List())') // test default value
      cy.contains('tr', 'children:').contains('button', 'add').click()
      cy.get('.value').should('have.text', 'Tree(2,List(Tree(2,List())))') // test nested default value
      cy.contains('tr', 'children:').contains('tr', 'children:').contains('button', 'add').click()
      cy.get('.value').should('have.text', 'Tree(2,List(Tree(2,List(Tree(2,List())))))') // test nested default value
      // edit value in the middle of nesting
      cy.get('> table:nth-child(2) > tr:nth-child(2) > td:nth-child(2) > div:nth-child(1) > div:nth-child(1) > table:nth-child(2) > tr:nth-child(1) > td:nth-child(2) > div:nth-child(1) > input:nth-child(1)').clear().type('5')
      cy.get('.value').should('have.text', 'Tree(2,List(Tree(5,List(Tree(2,List())))))')
    })
  })

  it('BinaryTree (recursive sealed trait)', () => {
      cy.get('.BinaryTree').within(($form) => {
      cy.get('select').select('Branch')
      cy.get('.value').should('have.text', 'Branch(Leaf(0),Leaf(0))')
      cy.contains('tr', 'right:').contains('select', 'Leaf').select('Branch')
      cy.contains('tr', 'left:').contains('select', 'Leaf').select('Branch')
      cy.get('.value').should('have.text', 'Branch(Branch(Leaf(0),Leaf(0)),Branch(Leaf(0),Leaf(0)))')
      cy.get('input[type="text"]').each((elem,index) => cy.wrap(elem).clear().type(index))
      cy.get('.value').should('have.text', 'Branch(Branch(Leaf(0),Leaf(1)),Branch(Leaf(2),Leaf(3)))')
    })
  })

  it('GenericLinkedList (generics)', () => {
    cy.get('.GenericLinkedList\\[Pet\\]').within(($form) => {
      cy.get('select').select('Cons')
      cy.get('.value').should('have.text', 'Cons(Cat(,4),Nil)')
      cy.contains('tr', 'tail:').within(($form) => {
        cy.get('select').select('Cons')
        cy.contains('tr', 'head:').within(($form) => {
          cy.get('select').select('Dog')
        })
      }) 

      cy.get('.value').should('have.text', 'Cons(Cat(,4),Cons(Dog(,true),Nil))')
    })
  })
})
