describe('empty spec', () => {
  it('Load demo', () => {
    cy.visit('http://localhost:12345')
  })

  it('String', () => {
    cy.get('.String input').clear().type('hello')
    cy.get('.String .value').should('have.text', 'hello')
    cy.get('.String input').clear().type('moon')
    cy.get('.String .value').should('have.text', 'moon')
  })

  it('Int', () => {
    cy.get('.Int input').clear().type('7')
    cy.get('.Int .value').should('have.text', '7')
    cy.get('.Int input').clear().type('3')
    cy.get('.Int .value').should('have.text', '3')
  })

  it('Double', () => {
    cy.get('.Double input').clear().type('2.3')
    cy.get('.Double .value').should('have.text', '2.3')
    cy.get('.Double input').clear().type('1e+230')
    cy.get('.Double .value').should('have.text', '1e+230')
  })

  it('Long', () => {
    cy.get('.Long input').clear().type('10000000000000')
    cy.get('.Long .value').should('have.text', '10000000000000')
    cy.get('.Long input').clear().type('22222222222222')
    cy.get('.Long .value').should('have.text', '22222222222222')
  })

  it('Boolean', () => {
    cy.get('.Boolean input').check()
    cy.get('.Boolean .value').should('have.text', 'true')
    cy.get('.Boolean input').uncheck()
    cy.get('.Boolean .value').should('have.text', 'false')
  })

  it('Option[Int]', () => {
    cy.get('.Option\\[Int\\] input[type="checkbox"]').check()
    cy.get('.Option\\[Int\\] .value').should('have.text', 'Some(0)')
    cy.get('.Option\\[Int\\] input[type="text"]').clear().type('13')
    cy.get('.Option\\[Int\\] .value').should('have.text', 'Some(13)')
    cy.get('.Option\\[Int\\] input[type="checkbox"]').uncheck()
    cy.get('.Option\\[Int\\] .value').should('have.text', 'None')
  })

  it('Seq[Int]', () => {
    cy.get('.Seq\\[Int\\]').contains('button','add').click()
    cy.get('.Seq\\[Int\\] .value').should('have.text', 'List(0)')
    cy.get('.Seq\\[Int\\] > div:nth-child(2) > div:nth-child(1) input[type="text"]').clear().type('13')
    cy.get('.Seq\\[Int\\] .value').should('have.text', 'List(13)')
    cy.get('.Seq\\[Int\\]').contains('button','add').click()
    cy.get('.Seq\\[Int\\] > div:nth-child(2) > div:nth-child(2) input[type="text"]').clear().type('4')
    cy.get('.Seq\\[Int\\] .value').should('have.text', 'List(13, 4)')
    cy.get('.Seq\\[Int\\] > div:nth-child(2) > div:nth-child(1) input[type="text"]').clear().type('14')
    cy.get('.Seq\\[Int\\] > div:nth-child(2) > div:nth-child(2) input[type="text"]').clear().type('5')
    cy.get('.Seq\\[Int\\] .value').should('have.text', 'List(14, 5)')
    cy.get('.Seq\\[Int\\] > div:nth-child(2) > div:nth-child(2) input[type="text"]').clear().type('5')
    cy.get('.Seq\\[Int\\] > div:nth-child(2) > div:nth-child(1)').contains('button', 'remove').click()
    cy.get('.Seq\\[Int\\] .value').should('have.text', 'List(5)')
    cy.get('.Seq\\[Int\\] > div:nth-child(2) > div:nth-child(1)').contains('button', 'remove').click()
    cy.get('.Seq\\[Int\\] .value').should('have.text', 'List()')
  })

  it('Person (case class)', () => {
    cy.get('.Person').contains('tr', 'name:').find('input[type="text"]').clear().type('Klaus')
    cy.get('.Person .value').should('have.text', 'Person(Klaus,5)') // test default value
    cy.get('.Person').contains('tr', 'age:').find('input[type="text"]').clear().type('7')
    cy.get('.Person .value').should('have.text', 'Person(Klaus,7)')
  })

  it('Pet (sealed trait)', () => {
    cy.get('.Pet').contains('tr', 'name:').find('input[type="text"]').clear().type('Tiger')
    cy.get('.Pet .value').should('have.text', 'Cat(Tiger,4)') // test default value
    cy.get('.Pet select').select('Dog')
    cy.get('.Pet .value').should('have.text', 'Dog(,true)') // test default value
  })
})
